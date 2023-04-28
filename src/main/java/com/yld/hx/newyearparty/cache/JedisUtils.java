package com.yld.hx.newyearparty.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.util.JedisClusterCRC16;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 韩旭
 * @Description 详细描述
 * @date 2022/5/1 16:57
 */
@Component
public class JedisUtils {

    /**
     * 单机redis使用的链接池
     */
    private JedisPool pool;
    /**
     * Redis Cluster模式时的地址列表
     */
    private Set<HostAndPort> nodes;
    /**
     * Redis Cluster模式时的链接池
     */
    private JedisCluster jedisCluster;
    /**
     * springboot autoconfig
     */
    @Autowired
    JedisConfig redisConfig;
    /**
     * IP和端口的正则表达式
     */
    static final Pattern PATTERN = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");

    @PostConstruct
    void init() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(redisConfig.getMaxIdel());
        poolConfig.setMaxTotal(redisConfig.getMaxActive());
        poolConfig.setMaxWaitMillis(redisConfig.getMaxWaitsMs());
        poolConfig.setMinIdle(redisConfig.getMinIdel());

        if (redisConfig.isClusterFlag()) {
            nodes = new HashSet<>();
            Matcher matcher = PATTERN.matcher(redisConfig.getHost());//正则方式提取服务地址列表
            while (matcher.find()) {
                HostAndPort hp = new HostAndPort(matcher.group(1), Integer.parseInt(matcher.group(2)));
                nodes.add(hp);
            }
            jedisCluster = new JedisCluster(nodes, 6000, poolConfig);
        } else {
            pool = new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(), 6000);
        }
    }

    public String get(String key) {
        try (Jedis resource = getResource(key)) {
            return resource.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public byte[] get(byte[] key) {
        try (Jedis resource = getResource(key)) {
            return resource.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public String set(String key, String value) {
        try (Jedis resource = getResource(key)) {
            return resource.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public String set(byte[] key, byte[] value) {
        try (Jedis resource = getResource(key)) {
            return resource.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public String setex(String key, String value, int seconds) {
        try (Jedis resource = getResource(key)) {
            return resource.setex(key, seconds, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public String setex(byte[] key, byte[] value, int seconds) {
        try (Jedis resource = getResource(key)) {
            return resource.setex(key, seconds, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long setnx(String key, String value) {
        try (Jedis resource = getResource(key)) {
            return resource.setnx(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long setnx(byte[] key, byte[] value) {
        try (Jedis resource = getResource(key)) {
            return resource.setnx(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long expire(String key, int seconds) {
        try (Jedis resource = getResource(key)) {
            return resource.expire(key, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long expire(byte[] key, int seconds) {
        try (Jedis resource = getResource(key)) {
            return resource.expire(key, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Boolean exists(String key) {
        try (Jedis resource = getResource(key)) {
            return resource.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Boolean exists(byte[] key) {
        try (Jedis resource = getResource(key)) {
            return resource.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public List<String> lrange(String key, int start, int stop) {
        try (Jedis resource = getResource(key)) {
            return resource.lrange(key, start, stop);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Map<String, String> hgetAll(String key) {
        try (Jedis resource = getResource(key)) {
            return resource.hgetAll(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Set<String> keys(String key) {
        try (Jedis resource = getResource(key)) {
            return resource.keys(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long sadd(String key, String... members){
        try (Jedis resource = getResource(key)) {
            return resource.sadd(key, members);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public String hmset(String key, Map<String, String> hash) {
        try (Jedis resource = getResource(key)) {
            return resource.hmset(key, hash);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long srem(String key, String value) {
        try (Jedis resource = getResource(key)) {
            return resource.srem(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long lpush(String key, String... members){
        try (Jedis resource = getResource(key)) {
            return resource.lpush(key, members);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    public Long del(String key) {
        try (Jedis resource = getResource(key)) {
            return resource.del(key);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("redis错误");
        }
    }

    /**
     * 获取jedis对象
     *
     * @param key 要操作的ke，redis cluster模式使用
     * @return
     */
    private Jedis getResource(String key) {
        if (redisConfig.isClusterFlag()) {
            return jedisCluster.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
        } else {
            return getResource();
        }
    }

    /**
     * 获取jedis对象
     *
     * @param key 要操作的ke，redis cluster模式使用
     * @return
     */
    private Jedis getResource(byte[] key) {
        if (redisConfig.isClusterFlag()) {
            return jedisCluster.getConnectionFromSlot(JedisClusterCRC16.getSlot(key));
        } else {
            return getResource();
        }
    }

    /**
     * 获取jedis对象
     *
     * @return
     */
    private Jedis getResource() {
        return pool.getResource();
    }

}
