package com.yld.hx.newyearparty.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.entity.StringEntity;

import com.alibaba.fastjson.JSONObject;
import com.yld.hx.newyearparty.cons.SystemCons;

public class MessageThread extends Thread {
	Log log = LogFactory.getLog("中奖消息发送线程");

	String lvl;
	String time;
	List<HashMap<String, String>> userInfos;
	String token;
	
	/**
	 * 
	 * @param lvl 中奖等级
	 * @param time 时间
	 * @param userInfos 人员信息List
	 * @param token
	 * 
	 * @see 模板格式：{{first.DATA}} 奖品名称：{{keyword1.DATA}} 中奖时间：{{keyword2.DATA}} {{remark.DATA}}
	 */
	public MessageThread(String lvl, String time, List<HashMap<String, String>> userInfos, String token) {
		this.lvl = lvl;
		this.time = time;
		this.userInfos = userInfos;
		this.token = token;
	}
	
	@Override
	public void run() {
		for (HashMap<String, String> userInfo : userInfos) {
			String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + token;
			
			Map<String, Object> msgMap = new HashMap<String, Object>();
			msgMap.put("touser", userInfo.get("openid"));
			msgMap.put("template_id", SystemCons.WECHAT_MSG_TID);
			msgMap.put("url", "");
			msgMap.put("topcolor", "#FF0000");
			
			Map<String, Object> data = new HashMap<String, Object>();
			
			Map<String, Object> first = new HashMap<String, Object>();
			first.put("value", "尊敬的["+userInfo.get("name")+"]，恭喜您中奖了！");
			first.put("color", "#173177");
			data.put("first", first);
			
			Map<String, Object> keyword1 = new HashMap<String, Object>();
			keyword1.put("value", lvl);
			keyword1.put("color", "#173177");
			data.put("keyword1", keyword1);
			
			Map<String, Object> keyword2 = new HashMap<String, Object>();
			keyword2.put("value", time);
			keyword2.put("color", "#173177");
			data.put("keyword2", keyword2);
			
			Map<String, Object> remark = new HashMap<String, Object>();
			remark.put("value", "请速到主席台领奖！");
			remark.put("color", "#173177");
			data.put("remark", remark);
			
			msgMap.put("data", data);
			
			String jsonString = JSONObject.toJSONString(msgMap);
			log.info("发送消息:" + jsonString);
			StringEntity json = new StringEntity(jsonString, "UTF-8");
			try {
				String post = HttpsUtils.post(url, null, null, json);
				log.info("消息响应：" + post);
			} catch (Exception e) {
				e.printStackTrace();
				log.warn("发送消息给:" + userInfo.get("openid") + "失败！");
			}
		}
		
	}

}
