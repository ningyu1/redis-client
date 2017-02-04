# redis-client
访问redis的java客户端，在jedis基础上包装，从coding方面优化使用方式 

1. pom中添加依赖
``` xml
<dependency>
  <groupId>com.jiuyescm.framework</groupId>
  <artifactId>redis-client</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. 删除原有的spring redis的xml配置

3. 引入新的spring redis xml配置
``` xml
<import resource="classpath*:spring-redis.xml" />
```

4. 增加redis.properties，如果已经有请忽略，需要的key值
```
redis.pool.maxTotal=1000
redis.pool.maxIdle=50
redis.pool.minIdle=10
redis.pool.testOnBorrow=true
redis.pool.testOnReturn=true
redis.ip=192.168.0.65
redis.port=6379
redis.timeout=2000
redis.password=Jy123456
```

5. 注入redisClient对象
``` java
@Autowired
public RedisClient redisClient;
```

6. 使用redisClient
``` java
redisClient.setex("key", ttl, "value");
redisClient.setnx("key", "value")
redisClient.set("key", "value");
redisClient.get("key");
```

7. monitor日志查看（debug）
```
2017-01-18 13:53:48.512 [main] DEBUG c.jiuyescm.framework.redis.client.aop.AdviceFilter - function: String redis.clients.jedis.JedisCommands.setex(String,int,String)
key: testeaaaaa
key type: String
key size: 10
return value type: String
return value size: 2
last: 40ms
2017-01-18 13:53:48.519 [main] DEBUG c.jiuyescm.framework.redis.client.aop.AdviceFilter - function: String redis.clients.jedis.JedisCommands.get(String)
key: testeaaaaa
key type: String
key size: 10
return value type: String
return value size: 9
last: 5ms
```