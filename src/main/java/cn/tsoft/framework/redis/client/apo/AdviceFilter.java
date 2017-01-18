/*
 * Copyright (c) 2016, Jiuye SCM and/or its affiliates. All rights reserved.
 * FileName: AdviceFilter.java
 * Author:   ningyu
 * Date:     2016年12月30日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.client.apo;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cn.tsoft.framework.redis.util.CacheUtils;

@Component
@Aspect
public class AdviceFilter {
	
    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(AdviceFilter.class);
    
    private static String RN = "\r\n";

	@Around("execution(* cn.tsoft.framework.redis.client.RedisClientImpl.*(..))")
    public Object process(ProceedingJoinPoint point) throws Throwable {
		Object[] args = point.getArgs();
		long begin = System.nanoTime();
		Object returnValue = point.proceed(args);
		long time = System.nanoTime() - begin;
		
		if(logger.isDebugEnabled()) {
			//访问目标方法的参数：
			String key = "";
			StringBuffer flumeLog = new StringBuffer();
			if(null!=args && args.length>0 &&null!=args[0]){
				if(args[0] instanceof String){
					key = (String)args[0];
				}
			}
			//key的大小监控日志
			//输出方法
			flumeLog.append("function: ").append(point.getSignature().toString()).append(RN);
			if (!StringUtils.isBlank(key)) {
				//输出key
				flumeLog.append("key: ").append(key).append(RN);
				//key大小
				flumeLog.append("key type: ").append(CacheUtils.getObjectType(key)).append(RN);
				flumeLog.append("key size: ").append(CacheUtils.getObjectSize(key)).append(RN);
			}
			//返回值大小
			flumeLog.append("return value type: ").append(CacheUtils.getObjectType(returnValue)).append(RN);
			flumeLog.append("return value size: ").append(CacheUtils.getObjectSize(returnValue)).append(RN);
			//时效
			flumeLog.append("last: ").append(time / 1000000).append("ms");
			
			logger.debug(flumeLog.toString());
		}

        return returnValue;
    }
	
}