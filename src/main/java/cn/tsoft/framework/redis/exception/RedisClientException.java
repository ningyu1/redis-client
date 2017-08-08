/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: RedisClientException.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.exception;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:36:47
 */
/**
 * @author ningyu
 *
 */
public class RedisClientException extends RuntimeException {

    /**
     */
    private static final long serialVersionUID = -4629579849260670181L;

    /**
     * 构造方法
     * 
     * @param msg 异常信息
     */
    public RedisClientException(String msg) {
        super(msg);
    }

    /**
     * 构造方法
     * 
     * @param exception 异常原因
     */
    public RedisClientException(Throwable exception) {
        super(exception);
    }

    /**
     * 构造方法
     * 
     * @param mag 异常信息
     * @param exception 异常原因
     */
    public RedisClientException(String mag, Exception exception) {
        super(mag, exception);
    }
}


