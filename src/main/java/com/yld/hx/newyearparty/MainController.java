package com.yld.hx.newyearparty;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yld.hx.newyearparty.cache.RedisUtils;
import com.yld.hx.newyearparty.cache.WechatCache;
import com.yld.hx.newyearparty.pojo.NPResponse;
import com.yld.hx.newyearparty.pojo.User;
import com.yld.hx.newyearparty.service.UserService;
import com.yld.hx.newyearparty.utils.CommonUtils;
import com.yld.hx.newyearparty.utils.HttpsUtils;
import com.yld.hx.newyearparty.utils.MessageThread;
import com.yld.hx.newyearparty.utils.SHA1;

import redis.clients.jedis.Jedis;

@RestController
@RequestMapping(value = "main")
public class MainController {
	Log log = LogFactory.getLog(MainController.class);

	@Autowired
	WechatCache wechatCache;

	@Autowired
	RedisUtils redis;

	@Autowired
	UserService userService;

	/**
	 * 获取微信jssdk注册配置，计算签名
	 * 
	 * @return
	 */
	@RequestMapping(value = "getJsSdkConfig", method = RequestMethod.GET)
	public NPResponse getJsSdkConfig(HttpServletRequest request, @RequestParam(name = "code") String code,
			@RequestParam(name = "state") String state) {
		NPResponse response = new NPResponse();

		log.info("获取jssdk配置签名信息");
		String noncestr = CommonUtils.getUUID();
		String jsapiTicket = wechatCache.getJsTicket();
		long timestamp = System.currentTimeMillis() / 1000;
		String url = "http://" + request.getServerName() + "/login_qrcode.html?code=" + code + "&state=" + state;

		String string1 = "jsapi_ticket=" + jsapiTicket + "&" + "noncestr=" + noncestr + "&" + "timestamp=" + timestamp
				+ "&" + "url=" + url;
		log.info(string1);
		String signature = SHA1.TOSHA1(string1);
		log.info(signature);

		String appId = wechatCache.getAppId();
		String appSecret = wechatCache.getAppSecret();

		response.setResponse("0000", "操作成功");
		response.put("noncestr", noncestr);
		response.put("timestamp", timestamp);
		response.put("signature", signature);
		response.put("appId", appId);

		response.putAll(getUserInfo(appId, appSecret, code));

		return response;
	}

	@RequestMapping(value = "login", method = RequestMethod.POST)
	public NPResponse login(HttpServletRequest request, @RequestBody User user) {
		NPResponse response = new NPResponse();
		log.info("人员签到！");
		log.info("人员信息：" + user.toString());
		String userKey = user.getName().trim() + "_" + user.getIdLast().trim().toUpperCase();
		Jedis jedis = redis.getJedis();

		String loginStopFlag = jedis.get("login_stop_flag");// 停止签到标识
		if (loginStopFlag != null && !"".equals(loginStopFlag)) {
			response.setResponse("9999", "签到入口已关闭");
			jedis.close();
			return response;
		}

		if (jedis.exists("login:" + userKey)) {
			response.setResponse("9999", "请不要重复签到");
			jedis.close();
			return response;
		}

		Map<String, String> allwoUserInfo = jedis.hgetAll("allwo:" + userKey);
		if (allwoUserInfo == null || allwoUserInfo.size() == 0) {
			log.error("不在人员名单中");
			response.setResponse("9999", "请确认您是否在人员名单中");
			jedis.close();
			return response;
		}
		log.info("开始校验经纬度:");
		String userLatitude = user.getLatitude();// 人员纬度
		String userLongitude = user.getLongitude();// 人员经度

		boolean inCircle = false;
		String localName = null;

		Set<String> locals = jedis.keys("local_*");// 允许签到坐标
		for (String local : locals) {
			Map<String, String> hgetAll = jedis.hgetAll(local);
			String latitude = hgetAll.get("latitude");// 纬度
			String longitude = hgetAll.get("longitude");// 经度
			inCircle = CommonUtils.isInCircle(1000, latitude, longitude, userLatitude, userLongitude);// 判断用户是否在签到地点500米内
			if (inCircle) {// 如果在范围之内
				localName = local.split("_")[1];// KEY编码是“local_地点名”，因为要记录登记地点，所以此处分割提取
				break;// 终止循环，减少空耗CPU
			}
		}
		if (!inCircle) {
			response.setResponse("9999", "请确认您是否在签到现场");
			jedis.close();
			return response;
		}

		log.info("开始存储人员签到信息");
		Map<String, String> hash = new HashMap<String, String>();
		hash.put("name", user.getName());// 姓名
		hash.put("idLast", user.getIdLast());// 证件后六位
		hash.put("openid", user.getOpenid());// OPENID
		hash.put("nickname", user.getNickname());// 微信昵称
		hash.put("headimgurl", user.getHeadimgurl());// 头像
		hash.put("loginTime", CommonUtils.getDate());// 登录时间
		hash.put("localName", localName);// 登录地点

		Long sadd = jedis.sadd("used_openid", user.getOpenid());
		if (sadd <= 0) {
			log.error(user.getName() + ":使用重复的微信号签到！");
			response.setResponse("9999", "您的微信号已经签到");
			jedis.close();
			return response;
		}

		String hset = jedis.hmset("login:" + userKey, hash);// 存入签到人员信息
		if (hset == null || "".equals(hset)) {
			log.error("存储人员签到信息时失败！");
			response.setResponse("9999", "存储签到信息时失败");
			jedis.srem("used_openid", user.getOpenid());
			jedis.close();
			return response;
		} else {
			Long lpush = jedis.lpush("login_user_list", "login:" + userKey);// 存储到签到人员列表
			if (lpush <= 0) {
				jedis.del("login:" + userKey);// 删除掉之前存储的信息
				log.error("签到人员加入到列表时失败！");
				response.setResponse("9999", "添加签到人员到列表时失败");
				jedis.srem("used_openid", user.getOpenid());
				jedis.close();
				return response;
			}
		}

		jedis.close();

		response.setResponse("0000", "签到成功");
		return response;
	}

	/**
	 * 根据Code获取客户信息
	 * 
	 * @param code
	 * @return
	 */
	private Map<String, String> getUserInfo(String appId, String appSecret, String code) {
		Map<String, String> retMap = new HashMap<String, String>();

		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret
				+ "&code=" + code + "&grant_type=authorization_code";
		String resJson;
		try {
			resJson = HttpsUtils.get(url);
		} catch (Exception e) {
			e.printStackTrace();
			return retMap;
		}
		if (resJson.indexOf("errcode") != -1) {
			log.error("获取user_info_token失败！返回信息：" + resJson);
			return retMap;
		}

		JSONObject parseObject = JSON.parseObject(resJson);
		String userToken = parseObject.getString("access_token");
		String openid = parseObject.getString("openid");

		String getInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + userToken + "&openid=" + openid
				+ "&lang=zh_CN";
		try {
			resJson = HttpsUtils.get(getInfoUrl);
		} catch (Exception e) {
			e.printStackTrace();
			return retMap;
		}
		if (resJson.indexOf("errcode") != -1) {
			log.error("获取user_info失败！返回信息：" + resJson);
			return retMap;
		}

		JSONObject userInfo = JSON.parseObject(resJson);
		retMap.put("openid", userInfo.getString("openid"));
		byte[] nameBytes;
		try {
			nameBytes = userInfo.getString("nickname").getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			log.error("用户微信昵称转码失败！");
			nameBytes = "无法获取".getBytes();
		}
		try {
			retMap.put("nickname", new String(nameBytes, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		retMap.put("sex", userInfo.getString("sex"));
		String headImgUrl = userInfo.getString("headimgurl");
		if (headImgUrl == null || "".equals(headImgUrl)) {
			headImgUrl = "img/localHead.jpg";
		}
		retMap.put("headimgurl", headImgUrl);

		log.info("获取user_info成功！" + resJson);
		return retMap;
	}

	/**
	 * 获取当前可以参加抽奖的人员 1.已经签到 2.还没有中奖
	 * 
	 * @return
	 */
	@RequestMapping(value = "getLoginUser", method = RequestMethod.POST)
	public NPResponse getLoginUser(HttpServletRequest request) {
		NPResponse response = new NPResponse();

		log.info("获取当前可以参加抽奖的人员");
		Jedis jedis = redis.getJedis();

		List<String> allowGameUser = userService.getAllowGameUser();// 获取可以参加抽奖的人

		List<HashMap<String, String>> userList = new ArrayList<HashMap<String, String>>();

		for (String userKey : allowGameUser) {
			HashMap<String, String> userInfo = (HashMap<String, String>) jedis.hgetAll(userKey);
			userList.add(userInfo);
		}
		jedis.close();
		response.setResponse("0000", "获取成功");
		response.put("userDatas", userList);
		return response;
	}

	/**
	 * 抽奖！ 1.已经签到 2.还没有中奖
	 * 
	 * @return
	 */
	@RequestMapping(value = "gameStart", method = RequestMethod.POST)
	public NPResponse gameStart(HttpServletRequest request, @RequestBody JSONObject jsonParam) {
		
		NPResponse response = new NPResponse();
		String date = CommonUtils.getDate();// 时间戳

		log.info("抽奖开始！");
		Jedis jedis = redis.getJedis();
		
		String adminCode = jedis.get("adminCode");
		String sessionAdminCode = (String)request.getSession().getAttribute("adminCode");
		if (sessionAdminCode == null || !sessionAdminCode.equals(adminCode)) {
			response.setResponse("9999", "鉴权失败！请确认是否进行管理员授权！");
			jedis.close();
			return response;
		}

		Integer pNum;
		try {
			pNum = jsonParam.getInteger("pnum");
		} catch (NumberFormatException e) {
			e.printStackTrace();
			response.setResponse("9999", "请输入正确的抽奖人数");
			jedis.close();
			return response;
		}
		String lvl = jsonParam.getString("lvl");// 抽奖等级

		List<String> allowGameUser = userService.getAllowGameUser();

		if (allowGameUser == null || allowGameUser.size() == 0) {
			response.setResponse("9999", "所有人都中奖了，如果还想发红包可以单独发给我");
			jedis.close();
			return response;
		}
		if (pNum > allowGameUser.size()) {
			pNum = allowGameUser.size();// 剩几个人就抽几个人吧
		}

		List<String> nbGuys = new ArrayList<String>();
		for (int i = 0; i < pNum; i++) {
			int n = new Random().nextInt(allowGameUser.size());
			String nbGuy = allowGameUser.get(n);// 取出这个人
			nbGuys.add(nbGuy);//
			allowGameUser.remove(nbGuy);// 去除这个人，虽然这么做效率很低，但是比较简答，此操作是为了下次随机抽奖不要再拿出重复的人
		}

		List<HashMap<String, String>> userList = new ArrayList<HashMap<String, String>>();
		StringBuffer userKeys = new StringBuffer();
		for (String userKey : nbGuys) {// 遍历中奖的人，获取他们的信息
			HashMap<String, String> userInfo = (HashMap<String, String>) jedis.hgetAll(userKey);
			userList.add(userInfo);
			userKeys.append(userKey + "|#|");
			jedis.lpush("ok_list", userKey);// 存储中奖人列表信息
		}
		Map<String, String> gameInfo = new HashMap<String, String>();// 记录开奖
		gameInfo.put("pnum", pNum + "");// 人数
		gameInfo.put("lvl", lvl);// 抽奖等级
		gameInfo.put("time", date);// 时间戳
		gameInfo.put("nbGuysId", userKeys.toString().substring(0, userKeys.length() - 3));// xxx|#|xxx|#|xxx|#|xxxx,去掉最后的一个|#|
		jedis.hmset("game:" + date, gameInfo);// 存储本次开奖整体信息

		jedis.lpush("game_list", "game:" + date);

		jedis.close();
		response.setResponse("0000", "抽奖成功");
		response.put("userDatas", userList);

		new MessageThread(lvl, date, userList, wechatCache.getToken()).start();// 由于微信模板消息发送响应速度不稳定，此处单独启动线程发送消息

		return response;
	}

	/**
	 * 获取历史抽奖信息
	 * 
	 * @return
	 */
	@RequestMapping(value = "getGames", method = RequestMethod.POST)
	public NPResponse getGames(HttpServletRequest request) {
		NPResponse response = new NPResponse();

		List<Map<String, Object>> games = new ArrayList<Map<String, Object>>();
		log.info("查询抽奖历史记录！");
		Jedis jedis = redis.getJedis();

		List<String> gameList = jedis.lrange("game_list", 0, -1);// 获取当前抽奖记录集合

		for (String gameKey : gameList) {
			Map<String, String> gameInfo = jedis.hgetAll(gameKey);
			Map<String, Object> obj = new HashMap<String, Object>();
			obj.putAll(gameInfo);// 先把抽奖信息存进去，但是由于抽奖信息中只存了人员ID，还需要重新获取一次中奖人信息以便显示

			String nbGuysId = gameInfo.get("nbGuysId");
			List<Map<String, String>> nbGuys = new ArrayList<Map<String, String>>();
			String[] split = nbGuysId.split("\\|\\#\\|");
			for (String key : split) {
				nbGuys.add((HashMap<String, String>) jedis.hgetAll(key));
			}
			obj.put("userInfos", nbGuys);

			games.add(obj);
		}

		jedis.close();
		response.setResponse("0000", "查询成功");
		response.put("gameDatas", games);
		return response;
	}

	/**
	 * 抽奖鉴权
	 * 
	 * @return
	 */
	@RequestMapping(value = "adminCheck", method = RequestMethod.POST)
	public NPResponse adminCheck(HttpServletRequest request, @RequestBody HashMap<String, String> retParam) {
		NPResponse response = new NPResponse();
		
		log.info("抽奖前鉴权！");
		Jedis jedis = redis.getJedis();
		String inputAdminCode = retParam.get("adminCode");

		String adminCode = jedis.get("adminCode");
		if (inputAdminCode.equals(adminCode)) {
			response.setResponse("0000", "鉴权成功");
			request.getSession().setAttribute("adminCode", adminCode);;
		} else {
			response.setResponse("9999", "鉴权码输入错误！");
		}
		jedis.close();
		return response;
	}

	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}
}
