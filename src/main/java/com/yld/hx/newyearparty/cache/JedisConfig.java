package com.yld.hx.newyearparty.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author 韩旭
 * @Description 详细描述
 * @date 2022/5/1 16:58
 */
@Component
public class JedisConfig {

    @Value("${spring.redis.cluster-flag:false}")
    private boolean clusterFlag;

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port:6379}")
    private int port;
    @Value("${spring.redis.jedis.pool.max-idle:10}")
    private int maxIdel;
    @Value("${spring.redis.jedis.pool.min-idle:10}")
    private int minIdel;
    @Value("${spring.redis.jedis.pool.max-active:10}")
    private int maxActive;
    @Value("${spring.redis.jedis.pool.max-wait-ms:1000}")
    private int maxWaitsMs;

    public boolean isClusterFlag() {
        return clusterFlag;
    }

    public void setClusterFlag(boolean clusterFlag) {
        this.clusterFlag = clusterFlag;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxIdel() {
        return maxIdel;
    }

    public void setMaxIdel(int maxIdel) {
        this.maxIdel = maxIdel;
    }

    public int getMinIdel() {
        return minIdel;
    }

    public void setMinIdel(int minIdel) {
        this.minIdel = minIdel;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWaitsMs() {
        return maxWaitsMs;
    }

    public void setMaxWaitsMs(int maxWaitsMs) {
        this.maxWaitsMs = maxWaitsMs;
    }

}
