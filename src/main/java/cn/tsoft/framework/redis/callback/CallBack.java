/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: CallBack.java
 * Author:   ningyu
 * Date:     2017年1月11日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.callback;

import redis.clients.jedis.Jedis;

/**
 * 回调
 * 
 * @author ningyu
 * @date 2017年1月11日 上午9:49:34
 */
public interface CallBack<T> {

	T invoke(Jedis jedis);
	
}


