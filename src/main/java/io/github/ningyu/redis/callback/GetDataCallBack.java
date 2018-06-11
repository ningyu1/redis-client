/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: GetDataCallBack.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package io.github.ningyu.redis.callback;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:32:49
 */
/**
 * @author ningyu
 * 
 */
public interface GetDataCallBack<R> {

    /**
     * ttl时间,不是所有命令都支持ttl设置
     * */
    int getExpiredTime();

    /**
     * 执行回调方法
     */
    R invoke();
}
