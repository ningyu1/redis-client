/*
 * Copyright (c) 2017, Jiuye SCM and/or its affiliates. All rights reserved.
 * FileName: RedisClientTest.java
 * Author:   ningyu
 * Date:     2017年1月18日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import cn.tsoft.framework.redis.client.RedisClient;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年1月18日 上午10:15:20
 */
@Component
public class RedisClientTest {
	
	@Autowired
	public RedisClient redisClient;

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
		RedisClientTest redisTest = (RedisClientTest) context.getBean("redisClientTest");
		redisTest.redisClient.setex("testeaaaaa", 100, "sdflkjsdf");
		
		System.out.println(redisTest.redisClient.get("testeaaaaa"));
	}
}


