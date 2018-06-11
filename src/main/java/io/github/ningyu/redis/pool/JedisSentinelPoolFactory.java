/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: JedisSentinelPool.java
 * Author:   ningyu
 * Date:     2017年9月20日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package io.github.ningyu.redis.pool;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

/**
 * <功能描述>
 * 
 * @author ningyu
 * @date 2017年9月20日 下午1:25:02
 */
public class JedisSentinelPoolFactory {
    
    public static final String COMMA = ",";
    
    String masterName;
    Set<String> sentinels;
    JedisPoolConfig poolConfig = new JedisPoolConfig();
    int timeout = Protocol.DEFAULT_TIMEOUT;
    String password;
    int database = Protocol.DEFAULT_DATABASE;
    String clientName;
    
    /**
     */
    public JedisSentinelPoolFactory() {
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }
    
    public void setSentinels(String sentinels) {
        String[] sentinel = StringUtils.split(sentinels, COMMA);
        Set<String> sentinelsSet = new HashSet<String>();
        for (int i = 0; i < sentinel.length; i++) {
            sentinelsSet.add(sentinel[i]);
        }
        this.sentinels = sentinelsSet;
    }
    
    public void setPoolConfig(JedisPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public void setDatabase(int database) {
        this.database = database;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public JedisSentinelPool create() {
        return new JedisSentinelPool(masterName, sentinels, poolConfig, timeout, timeout, password, database, clientName);
    }

}
