package com.yld.hx.newyearparty.cache;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component()
public class RedisUtils {

	JedisPool jedisPool;
	
	@Autowired
	private RedisConfig redisConfig;
	
	public static RedisUtils redisUtils;
	
	@PostConstruct
	public void init() {
		redisUtils = this;
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(200);
		config.setMaxIdle(5);
		config.setMaxWaitMillis(2000);
		
		jedisPool = new JedisPool(config, redisConfig.getRedisHost(), redisConfig.getPort(), 60, redisConfig.getRedisPass());
	}
	
	public Jedis getJedis() {
		return jedisPool.getResource();
	}
}
