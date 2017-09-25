# redis-client
访问redis的java客户端，在jedis基础上包装，从coding方面优化使用方式，增加namespace支持，可以根据项目或者产品做数据隔离

具体查看：https://ningyu1.github.io/site/post/22-redis-client/

### 1. pom中添加依赖
``` xml
<dependency>
  <groupId>cn.tsoft.framework</groupId>
  <artifactId>redis-client</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```

### 2. 引入新的spring redis xml配置
``` xml
<import resource="classpath:spring-redis.xml" />
```

### 3. 增加redis.properties，如果已经有请忽略，需要的key值
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

### 4. Api使用说明

ps.本次版本增加了namespace、泛型的支持（存、取直接使用java对象），namespace可以有效的避免key名称冲突和对以后做sharding提供了基础，泛型则是提升使用友好度，本次版本包装了驱动（jedis）的95%的方法，有一些性能不好的方法没有开放，新增了一些使用上更加友好的方法。

常规操作的command实现：RedisClientImpl

二进制操作的command实现：BinaryRedisClientImpl

两者都支持直接存、取java对象，区别在于前者序列化为json以string的方式发送到redis服务器，后者序列化为byte[]以字节方式发送到redis服务，通过redis-cli工具前者可以很明确的看到存的值，后者看到的是二进制编码。

### 5. 注入redisClient对象
``` java
@Autowired
public IRedisClient redisClient;
```

### 6. 回调接口

接口提供两个方法
`cn.tsoft.framework.redis.callback.GetDataCallBack<R>`

``` java
/**
  * ttl时间,不是所有命令都支持ttl设置
  * */
int getExpiredTime();
 
/**
  * 执行回调方法
  */
R invoke();
```

ps.int getExpiredTime();这个方法并不是所有命令都支持（hget系列不支持，因为hash的attr是不支持ttl设置的，ttl必须设置在hash的key上并不是hash的attr上），因此不支持ttl的命令就采用默认的空实现。在使用get*和hget*方法时，如果key返回为null，则通过该接口的`R invoke();`方法获取数据并放到redis中。hgetAllObjects方法上的GetDataCallBack<T> gbs参数是无效的传入null即可。如果在get方法获取不到值时不想走数据回调时传入null即可。

### 7. 使用示例：
``` java
//不设置回调
Metadata resule = redisClient.get(bizkey, nameSpace, Metadata.class, null);
 
List<Metadata > resule = redisClient.get(bizkey, nameSpace, new TypeReference<List<Metadata>>() {}, null);
 
//设置回调
List<Long> resule = redisClient.get(bizkey, nameSpace, new TypeReference<List<Long>>() {}, new GetDataCallBack<List<Long>>() {
            @Override
            public int getExpiredTime() {
                return 3600;
            }
            @Override
            public List<Long> invoke() {
                return getMetadataSourceProvider().getUserRoles(uid);
            }
        });
 
List<Long> resule = redisClient.hgetObject(bizkey, nameSpace, String.valueOf(uid), new TypeReference<List<Long>>() {}, new GetDataCallBack<List<Long>>() {
            @Override
            public int getExpiredTime() {
                return 0;
            }
            @Override
            public List<Long> invoke() {
                return getMetadataSourceProvider().getUserRoles(uid);
            }
        });
```

### 8. 参数说明

get*方法的参数Class<T> value和TypeReference<T> type的区别，前者不支持嵌套泛型，后者支持嵌套泛型，举一个例子说明
``` java
Metadata value = redisClient.get(bizkey, nameSpace, Metadata.class, null);
 
List<Metadata> list = redisClient.get(bizkey, nameSpace, new TypeReference<List<Metadata>>(){}, null);
```

### 9. 综合使用示例
``` java
redisClient.set(bizkey, namespace, new Metadata(), 60);//set并设置ttl60秒
redisClient.set(bizkey, namespace, new Metadata(), -1);//set不设置ttl
redisClient.setnx(bizkey, namespace, "aaaa");//key不存在时才设置值
redisClient.setex(bizkey, namespace, 60, new Metadata());//set一个key并设置ttl60秒，等价于第一行的用法
//setbit和setrange用法不多做说明，参考redis.io上面的command说明
 
redisClient.get(bizkey, namespace, new GetDataCallBack<String>(){
            @Override
            public int getExpiredTime() {
                return 60;
            }
            @Override
            public String invoke() {
                return "aaaa";
            }
});//获取，找不到取数据并set进去
 
redisClient.get(bizkey, namespace, Metadata.class, new GetDataCallBack<Metadata>(){
            @Override
            public int getExpiredTime() {
                return 60;
            }
            @Override
            public Metadata invoke() {
                return new Metadata();
            }
});//获取值，类型：Metadata
 
redisClient.get(bizkey, namespace, new TypeReference<List<Metadata.class>>(){}, new GetDataCallBack<List<Metadata>>(){
            @Override
            public int getExpiredTime() {
                return 60;
            }
            @Override
            public List<Metadata> invoke() {
                return new ArrayList<Metadata>;
            }
});//获取值，类型：List<Metadata>
 
redisClient.get(bizkey, namespace, new TypeReference<List<Metadata.class>>(){}, null);//获取值，类型：List<Metadata>
 
//getbit、getrange、getSet、hget、hgetAll、hgetObject、hgetAllObjects，用法不多做说明，参考redis.io上面的command说明
 
//管道，批量发送多条命令，但是不支持namespace需要手动添加namespace
Pipeline pipelined = redisClient.pipelined();
pipelined.set(key, value);
pipelined.get(key);
pipelined.syncAndReturnAll(); //发送命令并接受返回值
pipelined.sync();//发送命令不接受返回值
 
//其他z*、incr、decr、h*、s*命令不做说明，参考redis.io上面的command说明
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

**ps.替换掉以前的：`<import resource="classpath:spring-redis.xml" />`**

## 5. 注意事项

5.1. `pool`使用只允许使用一种，要么使用`jedis pool`要么使用`jedis sentinel pool`，两者不允许共存，`redisclient`启动会检测`pool`的设置是否合法，不合法会throw出异常，可能遇见的异常如下：

|异常|描述|解决办法|
|:---|:---|:------|
|RedisClientException("There can only be one pool! Will not work.")|只能存在一个pool的设置|检查xml配置，确定使用的pool，只允许保留一个pool设置，直接引用redis-client.jar中的（spring-redis.xml、spring-redis-sentinel.xml）可以解决这个问题|
|RedisClientException("No connection pool found! Will not work.")|没有找到pool的设置|检查xml配置，是否有pool的设置，直接引用redis-client.jar中的（spring-redis.xml、spring-redis-sentinel.xml）可以解决这个问题|

5.2. API使用起来跟以前没有任何变化，只是配置发生了变化
