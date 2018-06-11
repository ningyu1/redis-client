/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: BinaryRedisClientImpl.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package io.github.ningyu.redis.client.impl;

import io.github.ningyu.redis.callback.CallBack;
import io.github.ningyu.redis.client.IBinaryRedisClient;
import io.github.ningyu.redis.util.CacheUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:39:23
 */
/**
 * @author ningyu
 * 
 */
public class BinaryRedisClientImpl extends RedisClientImpl implements IBinaryRedisClient {

    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(BinaryRedisClientImpl.class);
    
    
    public BinaryRedisClientImpl(){
    }
    
    @Override
    public String setObject(final String bizkey,final String nameSpace, final Object value,
            final int expire) {
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);
        //避免setex和setnx问题
        String res = this.performFunction(key, new CallBack<String>() {
            @Override
            public String invoke(Jedis jedis) {
                try {
                    byte[] bkey = key.getBytes();
                    //修改原来逻辑key和field都不使用object2Array，使用
                    byte[] bvalue = CacheUtils.objectToByteArray(value);
                    return jedis.set(bkey, bvalue);
                } catch (IOException e) {
                    logger.error("key:"+key+",error:"+e.getMessage());
                }
                return null;
            }
        });
        //如果没有设置时间或者为负数，则不设置超时时间
        if(expire>0){
             this.performFunction(key, new CallBack<Long>() {
                // expire 
                public Long invoke(Jedis jedis) {
                    return jedis.expire(key,expire);
                }
            });        
        }
        return res;
    }

    @Override
    public Object getObject(final String bizkey,final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);
        return this.performFunction(key, new CallBack<Object>() {
            @Override
            public Object invoke(Jedis jedis) {
                try {
                    return CacheUtils.byteArrayToObject(jedis.get(key.getBytes()));
                } catch (IOException e) {
                     logger.error("key:"+key+",error:"+e.getMessage());
                }
                return null;
            }
        });
    }

    @Override
    public Long hsetObject(final String bizkey,final String nameSpace,final String field,
            final Object value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            @Override
            public Long invoke(Jedis jedis) {
                try {
                    byte[] bkey = key.getBytes();
                    byte[] bfield = field.getBytes();
                    byte[] bvalue = CacheUtils.objectToByteArray(value);
                    return jedis.hset(bkey, bfield, bvalue);
                } catch (IOException e) {
                     logger.error("key:"+key+",error:"+e.getMessage());
                }         
                return null;
            }
        });
    }

    @Override
    public Object hgetObject(final String bizkey,final String nameSpace,final String field) {
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);
        return this.performFunction(key, new CallBack<Object>() {
            @Override
            public Object invoke(Jedis jedis) {
                try {
                    byte[] bkey = key.getBytes();
                    byte[] bfield = field.getBytes();
                    return CacheUtils.byteArrayToObject(jedis.hget(bkey, bfield));
                } catch (IOException e) {
                    logger.error("key:"+key+",error:"+e.getMessage());
                }  
                return null;
            }
        });
    }

    @Override
    public Map<String, Object> hgetAllObjects(final String bizkey,final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);      
        return this.performFunction(key, new CallBack<Map<String, Object>>() {
            @Override
            public Map<String, Object> invoke(Jedis jedis) {
                try{
                    Map<byte[], byte[]> all = jedis.hgetAll(key.getBytes());
                    Map<String, Object> allObjs = new HashMap<String, Object>();
                    for(Entry<byte[], byte[]> item : all.entrySet()){
                        String _key = new String(item.getKey());
                        Object _value = CacheUtils.byteArrayToObject(item.getValue());
                        allObjs.put(_key, _value);
                    }
                    return allObjs;
                } catch(IOException e){
                    logger.error("key:"+key+",error:"+e.getMessage());
                }
                return null;
            }
        });
    }
    
    /**
     * 
     * 功能描述: 同hdel，只是操作的是object
     * 
     * @param bizkey 键值
     * @param namespace 命名空间
     * @param field hash field
     */
    @Override
    public void hdelObject(final String bizkey,final String nameSpace, final String... field){
        final String key = CacheUtils.getKeyByNamespace(bizkey,nameSpace);  
        this.performFunction(key, new CallBack<Long>() {
            @Override
            public Long invoke(Jedis jedis) {
                try{
                    byte[] bkey = key.getBytes();
                    byte[][] bfields = new byte[field.length][];
                    int i = 0;
                    for(String svalue:field){
                        if(StringUtils.isNotBlank(svalue)){
                            bfields[i++] = svalue.getBytes();
                        }
                    }
                    return jedis.hdel(bkey, bfields);
                } catch(Exception e){   
                    logger.error("key:"+key+",error:"+e.getMessage());
                }
                return 0L;
            }
        });
    }
}
