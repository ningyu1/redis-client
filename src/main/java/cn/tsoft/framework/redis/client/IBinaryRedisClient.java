/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: IBinaryRedisClient.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.client;

import java.util.Map;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:28:56
 */
/**
 * @author ningyu
 * 
 */
public interface IBinaryRedisClient extends IRedisClient {

    /**
     * 功能描述: 设置新对象
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @param value
     *            对象
     * @param expire
     *            过期时间
     * @return String 状态码
     */
    public String setObject(final String bizkey, final String nameSpace, final Object value, final int expire);

    /**
     * 
     * 功能描述: 用于访问JAVA的类对象的数据
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @return JAVA简单的类对象
     */
    Object getObject(final String bizkey, final String nameSpace);

    /**
     * 
     * 功能描述: 同hset，只是操作的是object
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @param field
     *            hash field
     * @param value
     *            值
     * @return Long
     */
    Long hsetObject(final String bizkey, final String nameSpace, final String field, final Object value);

    /**
     * 
     * 功能描述: 同hget，只是操作的是object
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @param field
     *            hash field
     * @return Object
     */
    Object hgetObject(final String bizkey, final String nameSpace, final String field);

    /**
     * 
     * 功能描述: 同hdel，只是操作的是object
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @param field
     *            hash field
     */
    void hdelObject(final String bizkey, final String nameSpace, final String... field);

    /**
     * 
     * 功能描述: 同hgetAll，只是操作的是object
     * 
     * @param bizkey
     *            键值
     * @param namespace
     *            命名空间
     * @return Map<String, Object>
     */
    Map<String, Object> hgetAllObjects(final String bizkey, final String nameSpace);
}
