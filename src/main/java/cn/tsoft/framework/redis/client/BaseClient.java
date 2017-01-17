/*
 * Copyright (c) 2017, Jiuye SCM and/or its affiliates. All rights reserved.
 * FileName: BaseClient.java
 * Author:   ningyu
 * Date:     2017年1月16日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年1月16日 下午3:01:10
 */
public abstract class BaseClient {
	
	private final Logger logger = LoggerFactory.getLogger(BaseClient.class.getName());

	@Autowired
	private JedisPool jedisPool;
	
	protected <T> T executeCache(CallBack<T> callback) {
		Jedis jedis = null;
		boolean broken = false;
		try {
            jedis = jedisPool.getResource();
            return callback.invoke(jedis);
        } catch (JedisException e) {
            if (jedis != null) {
                broken = true;
                jedisPool.returnBrokenResource(jedis);
            }
            logger.error("jedis pool get resource error:{}", e);
        } finally {
            if (jedis != null && !broken) {
                jedisPool.returnResource(jedis);
            }
        }
		return null;
	}
}


