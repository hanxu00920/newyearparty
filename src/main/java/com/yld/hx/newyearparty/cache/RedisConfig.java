package com.yld.hx.newyearparty.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * redis配置类
 * @author 67534
 *
 */
@Component("redisConfig")
public class RedisConfig {
	
	@Value("${yld.redis.database}")
	private String redisDatabase;
	
	@Value("${yld.redis.host}")
	private String redisHost;
	
	@Value("${yld.redis.port}")
	private int port;
	
	@Value("${yld.redis.password}")
	private String redisPass;

	public String getRedisDatabase() {
		return redisDatabase;
	}

	public void setRedisDatabase(String redisDatabase) {
		this.redisDatabase = redisDatabase;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRedisPass() {
		return redisPass;
	}

	public void setRedisPass(String redisPass) {
		this.redisPass = redisPass;
	}
	
}
