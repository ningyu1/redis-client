/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: RedisClientTest.java
 * Author:   ningyu
 * Date:     2017年1月18日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.test;

import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import cn.tsoft.framework.redis.client.IRedisClient;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年1月18日 上午10:15:20
 */
@Component
public class RedisClientTest {
	
	static AtomicInteger errorCount = new AtomicInteger(0); 
    static AtomicInteger successCount = new AtomicInteger(0); 
    static Vector<String> errorList = new Vector<String>();
	
	@Autowired
	public IRedisClient redisClient;

	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");
		RedisClientTest redisTest = (RedisClientTest) context.getBean("redisClientTest");
		
		ExecutorService pool = Executors.newFixedThreadPool(5);
        pool.execute(new Handler(redisTest.redisClient));
        pool.execute(new Handler(redisTest.redisClient));
        pool.execute(new Handler(redisTest.redisClient));
        pool.execute(new Handler(redisTest.redisClient));
        pool.execute(new Handler(redisTest.redisClient));
        pool.shutdown();
        while(true) {
            System.out.println("成功总数量："+successCount.get());
            System.out.println("失败总数量："+errorCount.get());
            for(String str : errorList) {
                System.out.println(str);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
		
//		redisTest.redisClient.setex("testeaaaaa", 100, "sdflkjsdf");
//		redisTest.redisClient.setnx("test1111a", "sdflkjsdf");
//		System.out.println(redisTest.redisClient.get("testeaaaaa"));
//		redisTest.redisClient.hset("hsetkey", "hsetattr", "value");
//		System.out.println(redisTest.redisClient.hget("hsetkey", "hsetattr"));
//		redisTest.redisClient.set("testaaaa", "namespace", "testaaaa", -1);
//		redisTest.redisClient.set("testaaaa", "namespace", "testaaaa111", -1);
//		redisTest.redisClient.set("testbbbb", "namespace", "testaaaa", -1);
//		redisTest.redisClient.setnx("testbbbb", "namespace", "testaaaa");
//		redisTest.redisClient.setex("testex", "namespace", 60, "testex");
//		Long ttl = redisTest.redisClient.ttl("testex", "namespace");
//		System.out.println(ttl);
//		redisTest.redisClient.patternDel("test*","namespace");
//		Pipeline p = redisTest.redisClient.pipelined();
		
	}
	
	static class Handler implements Runnable {
        private final IRedisClient redisClient;

        Handler(IRedisClient redisClient) {
            this.redisClient = redisClient;
        }

        public void run() {
//            for(int i=0;i<100;i++) {
            while(true) {
                String id = UUID.randomUUID().toString().replaceAll("-", "");
                try {
                  Long res = redisClient.setnx(id, "namespaces",id);
                  if(res == null) {
                      errorCount.incrementAndGet();
                      errorList.add(id);
                  }
                } catch(Exception e) {
                    System.out.println(Thread.currentThread().getName());
                    e.printStackTrace();
                    errorCount.incrementAndGet();
                    errorList.add(id);
                }
                try {
                    Thread.sleep((long)(java.lang.Math.random()*1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


