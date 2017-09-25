# redis-client
访问redis的java客户端，在jedis基础上包装，从coding方面优化使用方式 

### 1. pom中添加依赖
``` xml
<dependency>
  <groupId>cn.tsoft.framework</groupId>
  <artifactId>redis-client</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 删除原有的spring redis的xml配置

### 3. 引入新的spring redis xml配置
``` xml
<import resource="classpath*:spring-redis.xml" />
```

### 4. 增加redis.properties，如果已经有请忽略，需要的key值
```
redis.pool.maxTotal=1000
redis.pool.maxIdle=50
redis.pool.minIdle=10
redis.pool.testOnBorrow=true
redis.pool.testOnReturn=true
redis.ip=192.168.0.65
redis.port=6379
redis.timeout=2000
redis.password=123456
```

### 5. 注入redisClient对象
``` java
@Autowired
public RedisClient redisClient;
```

### 6. 使用redisClient
``` java
redisClient.setex("key", ttl, "value");
redisClient.setnx("key", "value")
redisClient.set("key", "value");
redisClient.get("key");
```

### 7. monitor日志查看（debug）
```
2017-01-18 13:53:48.512 [main] DEBUG c.tsoft.framework.redis.client.aop.AdviceFilter - function: String redis.clients.jedis.JedisCommands.setex(String,int,String)
key: testeaaaaa
key type: String
key size: 10
return value type: String
return value size: 2
last: 40ms
2017-01-18 13:53:48.519 [main] DEBUG c.tsoft.framework.redis.client.aop.AdviceFilter - function: String redis.clients.jedis.JedisCommands.get(String)
key: testeaaaaa
key type: String
key size: 10
return value type: String
return value size: 9
last: 5ms
```

----

以上是RedisClient操作单点Redis
以下是支持Sentinel（哨兵）+Redis集群的RedisClient（架构封装的Java访问Redis的客户端程序）高级使用方式

Redis集群方式：Master-Slave（1 - n 为一套集群可以多套）
Sentinel集群方式：Sentinel（n台，n>=3），投票人数：n-1（参与Master是否宕机以及下一任Master选举的投票人数）

## 1. Maven中引用（目前预览版）

```
<dependency>
  <groupId>cn.tsoft.framework</groupId>
  <artifactId>redis-client</artifactId>
  <version>1.2.0-SNAPSHOT</version>
</dependency>
```

## 2. 配置说明

原始（基础）配置：

```
redis.pool.maxTotal=1000
redis.pool.maxIdle=50
redis.pool.minIdle=10
redis.pool.testOnBorrow=true
redis.pool.testOnReturn=true
redis.ip=192.168.0.65
redis.port=6379
redis.timeout=2000
redis.password=123456
```

sentinel新增配置

```
# sentinel
redis.mastername=mymaster
redis.sentinels=127.0.0.1:26379,127.0.0.1:26380,127.0.0.1:26381
```

redis.mastername指的是monitor master的名称
redis.sentinels指的是哨兵的ip：port集合（ip和port需要替换）

删除配置

```
#redis.ip=192.168.0.65
#redis.port=6379
```

**ps.由于使用了sentinel自动发现redis服务因此不需要此配置，注释或删除即可**

## 3. spring配置说明

xml配置跟以前`pool`的配置方式有所不同，单节点`redis`的`pool`配置使用的是：`redis.clients.jedis.JedisPoolConfig`和`redis.clients.jedis.JedisPool`
`sentinel`的配置替换为：`redis.clients.jedis.JedisPoolConfig`和`cn.tsoft.framework.redis.pool.JedisSentinelPoolFactory`

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:tx="http://www.springframework.org/schema/tx"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
    http://www.springframework.org/schema/context  http://www.springframework.org/schema/context/spring-context.xsd
    http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd
    http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd">
     
    <aop:aspectj-autoproxy />
    <context:component-scan base-package="cn.tsoft.framework.redis" />
     
    <bean id="redisClient" class="cn.tsoft.framework.redis.client.impl.RedisClientImpl">
        <property name="jedisSentinelPoolFactory" ref="jedisSentinelPoolFactory" />
    </bean>
    <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxTotal" value="${redis.pool.maxTotal}" />
        <property name="maxIdle" value="${redis.pool.maxIdle}" />
        <property name="minIdle" value="${redis.pool.minIdle}" />
        <property name="testOnBorrow" value="${redis.pool.testOnBorrow}" />
        <property name="testOnReturn" value="${redis.pool.testOnReturn}" />
    </bean>
     
    <bean id="jedisSentinelPoolFactory" class="cn.tsoft.framework.redis.pool.JedisSentinelPoolFactory">
        <property name="poolConfig" ref="jedisPoolConfig" />
        <property name="masterName" value="${redis.mastername}" />
        <property name="sentinels" value="${redis.sentinels}" />
        <property name="timeout" value="${redis.timeout}" />
        <property name="password" value="${redis.password}" />
    </bean>
</beans>
```

**ps.以上配置在redis-client-1.2.0-SNAPSHOT.jar包的spring-redis-sentinel.xml文件中**

## 4. 项目中引用

```
<!-- redis.properties加载方式采用UCM的统一配置加载，具体可以查看global中的配置，如需要替换global的配置只需要在项目自定义配置中配置相同的key来进行属性覆盖  -->
<context:component-scan base-package="cn.tsoft.framework.redis" />
<import resource="classpath:spring-redis-sentinel.xml" />
```

**ps.替换掉以前的：<import resource="classpath:spring-redis.xml" />**

## 5. 注意事项

5.1. `pool`使用只允许使用一种，要么使用`jedis pool`要么使用`jedis sentinel pool`，两者不允许共存，`redisclient`启动会检测`pool`的设置是否合法，不合法会throw出异常，可能遇见的异常如下：

|异常|描述|解决办法|
|:---|:---|:------|
|RedisClientException("There can only be one pool! Will not work.")|只能存在一个pool的设置|检查xml配置，确定使用的pool，只允许保留一个pool设置，直接引用redis-client.jar中的（spring-redis.xml、spring-redis-sentinel.xml）可以解决这个问题|
|RedisClientException("No connection pool found! Will not work.")|没有找到pool的设置|检查xml配置，是否有pool的设置，直接引用redis-client.jar中的（spring-redis.xml、spring-redis-sentinel.xml）可以解决这个问题|

5.2. API使用起来跟以前没有任何变化，只是配置发生了变化
