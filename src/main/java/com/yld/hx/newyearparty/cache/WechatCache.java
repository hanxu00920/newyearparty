package com.yld.hx.newyearparty.cache;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yld.hx.newyearparty.cons.SystemCons;
import com.yld.hx.newyearparty.utils.HttpsUtils;

import redis.clients.jedis.Jedis;

@Component("wechatCache")
public class WechatCache {

    Log log = LogFactory.getLog(WechatCache.class);

    @Autowired
    JedisUtils jedis;

    @PostConstruct
    public void init() {
        log.info("初始化微信缓存参数");
        log.info("获取jedis");
        try {
            log.info("检查token");
            String token = jedis.get(SystemCons.WECHAT_TOKEN);
            if (token == null || "".equals(token)) {
                log.error("没有token，调用微信接口获取token");
                initToken();
            }

            log.info("检查jsapi_ticket");
            String jsapiTicket = jedis.get(SystemCons.WECHAT_JS_TICKET);
            if (jsapiTicket == null || "".equals(jsapiTicket)) {
                log.error("没有jsapi_ticket，调用微信接口获取jsapi_ticket");
                initJsTicket();
            }

            log.info("初始化微信缓存成功！token[" + jedis.get(SystemCons.WECHAT_TOKEN) + "] jsticket[" + jedis.get(SystemCons.WECHAT_JS_TICKET) + "]");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void initToken() throws Exception {
        try {
            log.info("开始调用微信公众平台获取token");
            String appid = jedis.get(SystemCons.WECHAT_APPID);
            String appsecret = jedis.get(SystemCons.WECHAT_APPSECRET);

            if (appid == null || "".equals(appid)) {
                log.error("没有在系统中维护appid，请维护！");
                throw new Exception("没有在系统中维护appid，请维护！");
            } else if (appsecret == null || "".equals(appsecret)) {
                log.error("没有在系统中维护appsecret，请维护！");
                throw new Exception("没有在系统中维护appsecret，请维护！");
            }

            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appid
                    + "&secret=" + appsecret;
            String resJson = HttpsUtils.get(url);
            if (resJson.indexOf("errcode") != -1) {
                log.error("获取token失败！返回信息：" + resJson);
                throw new Exception("获取token失败！");
            }

            JSONObject parseObject = JSON.parseObject(resJson);
            String token = parseObject.getString("access_token");
            int tokenExpires = parseObject.getInteger("expires_in");

            jedis.set(SystemCons.WECHAT_TOKEN, token);
            jedis.expire(SystemCons.WECHAT_TOKEN, tokenExpires);
            log.info("获取token成功！" + resJson);

        } catch (Exception e) {
            throw e;
        }
    }


    public void initJsTicket() throws Exception {
        try {
            log.info("开始调用微信公众平台获取jsapi_ticket");
            String token = jedis.get(SystemCons.WECHAT_TOKEN);

            if (token == null || "".equals(token)) {
                log.error("系统中没有可用的token！");
                initToken();//重新尝试初始化token
                token = jedis.get(SystemCons.WECHAT_TOKEN);
                if (token == null || "".equals(token)) {
                    log.error("重新初始化token失败！");
                    throw new Exception("重新初始化token失败！");
                }
            }

            String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + token + "&type=jsapi";
            String resJson = HttpsUtils.get(url);

            JSONObject parseObject = JSON.parseObject(resJson);
            int errcode = parseObject.getIntValue("errcode");
            String errmsg = parseObject.getString("errmsg");

            if (errcode != 0) {
                log.error("获取jsapi_ticket出错！错误信息：" + errmsg);
                throw new Exception("获取jsapi_ticket出错！");
            }
            String ticket = parseObject.getString("ticket");
            int ticketExpires = parseObject.getInteger("expires_in");

            jedis.set(SystemCons.WECHAT_JS_TICKET, ticket);
            jedis.expire(SystemCons.WECHAT_JS_TICKET, ticketExpires);
            log.info("获取jsapi_ticket成功！" + resJson);

        } catch (Exception e) {
            throw e;
        }
    }

    public String getToken() {
        String token = null;
        try {
            token = jedis.get(SystemCons.WECHAT_TOKEN);
            if (token == null || "".equals(token)) {
                this.initToken();
                token = jedis.get(SystemCons.WECHAT_TOKEN);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    public String getAppId() {
        String appId = null;
        try {
            appId = jedis.get(SystemCons.WECHAT_APPID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appId;
    }

    public String getAppSecret() {
        String appSecret = null;
        try {
            appSecret = jedis.get(SystemCons.WECHAT_APPSECRET);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appSecret;
    }

    public String getJsTicket() {
        String jsTicket = null;
        try {
            jsTicket = jedis.get(SystemCons.WECHAT_JS_TICKET);
            if (jsTicket == null || "".equals(jsTicket)) {
                this.initJsTicket();
                jsTicket = jedis.get(SystemCons.WECHAT_JS_TICKET);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsTicket;
    }

}
