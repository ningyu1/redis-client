# redis-client
访问redis的java客户端，在jedis基础上包装，从coding方面优化使用方式，增加namespace支持，可以根据项目或者产品做数据隔离

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
redis.password=Jy123456
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
`com.jiuyescm.framework.redis.callback.GetDataCallBack<R>`

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
