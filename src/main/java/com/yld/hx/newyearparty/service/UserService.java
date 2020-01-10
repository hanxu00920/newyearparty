package com.yld.hx.newyearparty.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yld.hx.newyearparty.cache.RedisUtils;
import com.yld.hx.newyearparty.cache.WechatCache;

import redis.clients.jedis.Jedis;

@Service
public class UserService {
	Log log = LogFactory.getLog(UserService.class);

	@Autowired
	WechatCache wechatCache;
	
	@Autowired
	RedisUtils redis;
	
	/**
	 * 获取可以参加抽奖游戏的人员
	 * @return
	 */
	public List<String> getAllowGameUser() {
		Jedis jedis = redis.getJedis();
		
		List<String> loginUserList = jedis.lrange("login_user_list", 0, -1);//获取当前签到人员集合
		List<String> okList = jedis.lrange("ok_list", 0, -1);//获取当前中奖人员记录
		
		loginUserList.removeAll(okList);//去除已经中奖的人
		
		jedis.close();
		return loginUserList;
	}

	
}
