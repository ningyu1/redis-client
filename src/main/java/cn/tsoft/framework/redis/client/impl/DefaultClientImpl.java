/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: BaseClient.java
 * Author:   ningyu
 * Date:     2017年1月16日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;
import cn.tsoft.framework.redis.callback.CallBack;
import cn.tsoft.framework.redis.exception.RedisClientException;
import cn.tsoft.framework.redis.pool.JedisSentinelPoolFactory;

/**
 * 
 * @author ningyu
 * @date 2017年1月16日 下午3:01:10
 */
public class DefaultClientImpl implements InitializingBean, DisposableBean {

private final Logger logger = LoggerFactory.getLogger(DefaultClientImpl.class.getName());
    
    private Pool<Jedis> pool;

    private JedisPool jedisPool;
    
    private JedisSentinelPool jedisSentinelPool;
    
    private JedisSentinelPoolFactory jedisSentinelPoolFactory;

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }
    
    public void setJedisSentinelPoolFactory(JedisSentinelPoolFactory jedisSentinelPoolFactory) {
        this.jedisSentinelPoolFactory = jedisSentinelPoolFactory;
        createJedisSentinelPool();
    }
    
    public void createJedisSentinelPool() {
        this.jedisSentinelPool = this.jedisSentinelPoolFactory.create();
    }

    protected <T> T execute(CallBack<T> callback) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return callback.invoke(jedis);
        } catch (JedisException e) {
            logger.error("jedis pool get resource error:{}", e);
        } finally {
            if (null != jedis) {
                jedis.close();
            }
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {
        if (null != jedisPool) {
            jedisPool.close();
        }
        if (null != jedisSentinelPool) {
            jedisSentinelPool.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (null == jedisPool && null == jedisSentinelPool) {
            throw new RedisClientException("No connection pool found! Will not work.");
        }
        if (null != jedisPool && null != jedisSentinelPool) {
            throw new RedisClientException("There can only be one pool! Will not work.");
        }
        if (null != jedisPool) {
            pool = jedisPool;
            return;
        }
        if (null != jedisSentinelPool) {
            pool = jedisSentinelPool;
        }
    }
}
