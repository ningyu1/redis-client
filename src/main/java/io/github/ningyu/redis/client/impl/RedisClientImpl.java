/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: RedisClientImpl.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package io.github.ningyu.redis.client.impl;

import io.github.ningyu.redis.callback.CallBack;
import io.github.ningyu.redis.callback.GetDataCallBack;
import io.github.ningyu.redis.client.IRedisClient;
import io.github.ningyu.redis.util.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;

import com.alibaba.fastjson.TypeReference;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:40:21
 */
/**
 * @author ningyu
 * 
 */
public class RedisClientImpl extends DefaultClientImpl implements IRedisClient {

    /**
     * 日志记录
     */
    private static Logger logger = LoggerFactory.getLogger(RedisClientImpl.class);

    public RedisClientImpl() {
    }

    /**
     * 
     * @param key
     *            key value
     * @param callBack
     *            回调方法
     * @param <R>
     *            泛型对象
     * @return 结果集合
     */
    protected <R> R performFunction(String entrykey, CallBack<R> callBack) {
        return execute(callBack);
    }

    /**
     * @param key
     *            主键
     * @param value
     *            值
     * @param time
     *            seconds
     * @return Bulk reply
     */
    @Override
    public String set(final String bizkey, final String nameSpace, final String value, final int time) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        // 避免setex和setnx问题
        String res = this.performFunction(key, new CallBack<String>() {
            // set
            public String invoke(Jedis jedis) {
                // 代码合并，避免多次调用，走不同代理有时间差的问题
                String res = jedis.set(key, value);
                // 如果没有设置时间或者为负数，则不设置超时时间
                if (time > 0) {
                    Long resLong = jedis.expire(key, time);
                    if (resLong != 1) {
                        logger.error("key:" + key + "expire exception!!!");
                    }
                }
                return res;
            }
        });
        return res;
    }

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * 
     * @param key
     * @param value
     * @param nxxx
     *            NX|XX, NX -- Only set the key if it does not already exist. XX
     *            -- Only set the key if it already exist.
     * @param expx
     *            EX|PX, expire time units: EX = seconds; PX = milliseconds
     * @param time
     *            expire time in the units of {@param #expx}
     * @return Status code reply
     */
    @Override
    public String set(final String bizkey, final String nameSpace, final String value, final String nxxx,
            final String expx, final long time) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public String get(final String bizkey, final String nameSpace, final GetDataCallBack<String> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                String res = jedis.get(key);
                if (StringUtils.isEmpty(res)) {
                    if (null != gbs) {
                        res = gbs.invoke();
                        if (StringUtils.isNotEmpty(res)) {
                            set(bizkey, nameSpace, res, "NX", "EX", gbs.getExpiredTime());
                        }
                    }
                }
                return res;
            }
        });
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "1" is returned. Note that even keys set with an empty
     * string as value will return "0".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @return Boolean reply, true if the key exists, otherwise false
     */
    @Override
    public Boolean exists(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */
    @Override
    public String type(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.type(key);
            }
        });
    }

    /**
     * Set a timeout on the specified key. After the timeout the key will be
     * automatically deleted by the server. A key with an associated timeout is
     * said to be volatile in Redis terminology.
     * <p>
     * Voltile keys are stored on disk like the other keys, the timeout is
     * persistent too like all the other aspects of the dataset. Saving a
     * dataset containing expires and stopping the server does not stop the flow
     * of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     *            specified key
     * @param seconds
     *            associated timeout
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    @Override
    public Long expire(final String bizkey, final String nameSpace, final int seconds) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }

    /**
     * EXPIREAT works exctly like {@link #expire(String, int) EXPIRE} but
     * instead to get the number of seconds representing the Time To Live of the
     * key as a second argument (that is a relative way of specifing the TTL),
     * it takes an absolute one in the form of a UNIX timestamp (Number of
     * seconds elapsed since 1 Gen 1970).
     * <p>
     * EXPIREAT was introduced in order to implement the Append Only File
     * persistence mode so that EXPIRE commands are automatically translated
     * into EXPIREAT commands for the append only file. Of course EXPIREAT can
     * also used by programmers that need a way to simply specify that a given
     * key should expire at a given time in the future.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     *            specified key
     * @param unixTime
     *            UNIX timestamp
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    @Override
    public Long expireAt(final String bizkey, final String nameSpace, final long unixTime) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.expireAt(key, unixTime);
            }
        });
    }

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(String, int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param key
     *            specified key
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */
    @Override
    public Long ttl(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.ttl(key);
            }
        });
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     *            specified key
     * @param offset
     *            offset
     * @param value
     *            string value
     * @return result
     */
    @Override
    public Boolean setbit(final String bizkey, final String nameSpace, final long offset, final boolean value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.setbit(key, offset, value);
            }
        });
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     *            specified key
     * @param offset
     *            offset
     * @return result
     */
    @Override
    public Boolean getbit(final String bizkey, final String nameSpace, final long offset) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.getbit(key, offset);
            }
        });
    }

    /**
     * setrange
     * 
     * @param key
     *            specified key
     * @param offset
     *            offset
     * @param value
     *            string value
     * @return result
     * 
     */
    @Override
    public Long setrange(final String bizkey, final String nameSpace, final long offset, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.setrange(key, offset, value);
            }
        });
    }

    /**
     * setrange
     * 
     * @param key
     *            specified key
     * @param startOffset
     *            start offset
     * @param endOffset
     *            end Offset
     * @return result
     * 
     */
    @Override
    public String getrange(final String bizkey, final String nameSpace, final long startOffset, final long endOffset) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.getrange(key, startOffset, endOffset);
            }
        });
    }

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @param value
     *            the old value
     * @return Bulk reply
     */
    @Override
    public String getSet(final String bizkey, final String nameSpace, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.getSet(key, value);
            }
        });
    }

    /**
     * SETNX works exactly like {@link #set(String, String) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @param value
     *            value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */
    @Override
    public Long setnx(final String bizkey, final String nameSpace, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     *            specified key
     * @param seconds
     *            timeout seconds
     * @param value
     *            value
     * @return Bulk reply
     */
    @Override
    public String setex(final String bizkey, final String nameSpace, final int seconds, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }

    /**
     * IDECRBY work just like {@link #decr(String) INCR} but instead to
     * decrement by 1 the decrement is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #incrBy(String, int)
     * 
     * @param key
     *            specified key
     * @param integer
     *            integer value
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    @Override
    public Long decrBy(final String bizkey, final String nameSpace, final long integer) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.decrBy(key, integer);
            }
        });
    }

    /**
     * Decrement the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the decrement operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #incrBy(String, int)
     * @see #decrBy(String, int)
     * 
     * @param key
     *            specified key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    @Override
    public Long decr(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    /**
     * INCRBY work just like {@link #incr(String) INCR} but instead to increment
     * by 1 the increment is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #decrBy(String, int)
     * 
     * @param key
     *            specified key
     * @param integer
     *            integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    @Override
    public Long incrBy(final String bizkey, final String nameSpace, final long integer) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.incrBy(key, integer);
            }
        });
    }

    /**
     * Increment the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the increment operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incrBy(String, int)
     * @see #decr(String)
     * @see #decrBy(String, int)
     * 
     * @param key
     *            specified key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    @Override
    public Long incr(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    /**
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     *            specified key
     * @param start
     *            start
     * @param end
     *            end
     * @return Bulk reply
     */
    @Override
    public String substr(final String bizkey, final String nameSpace, final int start, final int end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.substr(key, start, end);
            }
        });
    }

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            hash field
     * @param value
     *            specified value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    @Override
    public Long hset(final String bizkey, final String nameSpace, final String field, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            hash field
     * @return Bulk reply
     */
    @Override
    public String hget(final String bizkey, final String nameSpace, final String field,
            final GetDataCallBack<String> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                String res = jedis.hget(key, field);
                if (StringUtils.isEmpty(res)) {
                    if (null != gbs) {
                        res = gbs.invoke();
                        if (StringUtils.isNotEmpty(res)) {
                            hset(bizkey, nameSpace, field, res);
                        }
                    }
                }
                return res;
            }
        });
    }

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            hash field
     * @param value
     *            specified value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */
    @Override
    public Long hsetnx(final String bizkey, final String nameSpace, final String field, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.hsetnx(key, field, value);
            }
        });
    }

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     *            specified key
     * @param hash
     *            hash
     * @return Return OK or Exception if hash is empty
     */
    @Override
    public String hmset(final String bizkey, final String nameSpace, final Map<String, String> hash) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     *            specified key
     * @param fields
     *            specified fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */
    @Override
    public List<String> hmget(final String bizkey, final String nameSpace, final String... fields) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key
     * does not exist, a new key holding a hash is created. If field does not
     * exist or holds a string, the value is set to 0 before applying the
     * operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed
     * integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            specified key
     * @param value
     *            specified value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */
    @Override
    public Long hincrBy(final String bizkey, final String nameSpace, final String field, final long value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.hincrBy(key, field, value);
            }
        });
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            specified key
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */
    @Override
    public Boolean hexists(final String bizkey, final String nameSpace, final String field) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.hexists(key, field);
            }
        });
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param field
     *            specified field
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */
    @Override
    public Long hdel(final String bizkey, final String nameSpace, final String... field) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.hdel(key, field);
            }
        });
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */
    @Override
    public Long hlen(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }

    /**
     * @param key
     *            specified key
     * @return result
     */
    @Override
    public Set<String> hkeys(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }

    /**
     * @param key
     *            specified key
     * @return result
     */
    @Override
    public List<String> hvals(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.hvals(key);
            }
        });
    }

    /**
     * @param key
     *            specified key
     * @return result
     */
    @Override
    public Map<String, String> hgetAll(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Map<String, String>>() {
            public Map<String, String> invoke(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    /*
     * {@inheritDoc}
     * 
     * @Override public Long rpush(final String bizkey,final String nameSpace,
     * final String value) { return this.performFunction(key, new
     * CallBack<Long>() { public Long invoke(Jedis jedis) { return
     * jedis.rpush(key, value); } }); }
     * 
     * 存入值
     * 
     * @param key 键
     * 
     * @param value 值
     * 
     * @return 存入值
     * 
     * @Override public Long lpush(final String bizkey,final String nameSpace,
     * final String value) { return this.performFunction(key, new
     * CallBack<Long>() { public Long invoke(Jedis jedis) { return
     * jedis.lpush(key, value); } }); }
     */

    /**
     * 获取长度
     * 
     * @param key
     *            键
     * @return 长度
     */
    @Override
    public Long llen(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }

    /**
     * 根据键值，起始位置获取结果列表
     * 
     * @param key
     *            键值
     * @param start
     *            开始位置
     * @param end
     *            结束为止
     * @return 结果列表
     */
    @Override
    public List<String> lrange(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.lrange(key, start, end);
            }
        });
    }

    /**
     * 根据键值，起始位置trim字符串
     * 
     * @param key
     *            键值
     * @param start
     *            开始位置
     * @param end
     *            结束位置
     * @return trim后的字符串
     */
    @Override
    public String ltrim(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.ltrim(key, start, end);
            }
        });
    }

    /**
     * 获取指定索引的字符串
     * 
     * @param key
     *            键值
     * @param index
     *            索引
     * @return 指定索引的字符串
     */
    @Override
    public String lindex(final String bizkey, final String nameSpace, final long index) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.lindex(key, index);
            }
        });
    }

    /**
     * 设置指定索引值
     * 
     * @param key
     *            键值
     * @param index
     *            索引
     * @param value
     *            值
     * @return result
     */
    @Override
    public String lset(final String bizkey, final String nameSpace, final long index, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.lset(key, index, value);
            }
        });
    }

    /**
     * Remove the first count occurrences of the value element from the list. If
     * count is zero all the elements are removed. If count is negative elements
     * are removed from tail to head, instead to go from head to tail that is
     * the normal behaviour. So for example LREM with count -2 and hello as
     * value to remove against the list (a,b,c,hello,x,hello,hello) will lave
     * the list (a,b,c,hello,x). The number of removed elements is returned as
     * an integer, see below for more information about the returned value. Note
     * that non existing keys are considered like empty lists by LREM, so LREM
     * against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     * 
     * @param key
     *            specified key
     * @param count
     *            removed counts
     * @param value
     *            value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */
    @Override
    public Long lrem(final String bizkey, final String nameSpace, final long count, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.lrem(key, count, value);
            }
        });
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #rpop(String)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public String lpop(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.lpop(key);
            }
        });
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #lpop(String)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public String rpop(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.rpop(key);
            }
        });
    }

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */
    @Override
    public Long sadd(final String bizkey, final String nameSpace, final String... member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.sadd(key, member);
            }
        });
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param key
     *            specified key
     * @return Multi bulk reply
     */
    @Override
    public Set<String> smembers(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
    }

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    @Override
    public Long srem(final String bizkey, final String nameSpace, final String... member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.srem(key, member);
            }
        });
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public String spop(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.spop(key);
            }
        });
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public Long scard(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.scard(key);
            }
        });
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */
    @Override
    public Boolean sismember(final String bizkey, final String nameSpace, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.sismember(key, member);
            }
        });
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public String srandmember(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.srandmember(key);
            }
        });
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @return Bulk reply
     */
    @Override
    public List<String> srandmember(final String bizkey, final String nameSpace, final int count) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.srandmember(key, count);
            }
        });
    }

    /**
     * Add the specified member having the specifeid score to the sorted set
     * stored at key. If member is already a member of the sorted set the score
     * is updated, and the element reinserted in the right position to ensure
     * sorting. If key does not exist a new sorted set with the specified member
     * as sole member is crated. If the key exists but does not hold a sorted
     * set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     *            specified key
     * @param score
     *            specifeid score
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */
    @Override
    public Long zadd(final String bizkey, final String nameSpace, final double score, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zadd(key, score, member);
            }
        });
    }

    /**
     * @param key
     *            specefied key
     * @param start
     *            specified start member
     * @param end
     *            specified end member
     * @return result
     */
    @Override
    public Set<String> zrange(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }

    /**
     * Remove the specified member from the sorted set value stored at key. If
     * member was not a member of the set no operation is performed. If key does
     * not not hold a set value an error is returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * 
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    @Override
    public Long zrem(final String bizkey, final String nameSpace, final String... member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zrem(key, member);
            }
        });
    }

    /**
     * If member already exists in the sorted set adds the increment to its
     * score and updates the position of the element in the sorted set
     * accordingly. If member does not already exist in the sorted set it is
     * added with increment as score (that is, like if the previous score was
     * virtually zero). If key does not exist a new sorted set with the
     * specified member as sole member is crated. If the key exists but does not
     * hold a sorted set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number. It's possible to provide a negative value to
     * perform a decrement.
     * <p>
     * For an introduction to sorted sets check the Introduction to Redis data
     * types page.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     *            specified key
     * @param score
     *            specified score
     * @param member
     *            specified member
     * @return The new score
     */
    @Override
    public Double zincrby(final String bizkey, final String nameSpace, final double score, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Double>() {
            public Double invoke(Jedis jedis) {
                return jedis.zincrby(key, score, member);
            }
        });
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from low to high.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrevrank(String, String)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    @Override
    public Long zrank(final String bizkey, final String nameSpace, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zrank(key, member);
            }
        });
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from high to low.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrank(String, String)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    @Override
    public Long zrevrank(final String bizkey, final String nameSpace, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zrevrank(key, member);
            }
        });
    }

    /**
     * zrevrange
     * 
     * @param key
     *            specified key
     * @param start
     *            specified start member
     * @param end
     *            specified end member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */
    @Override
    public Set<String> zrevrange(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrevrange(key, start, end);
            }
        });
    }

    /**
     * zrangeWithScores
     * 
     * @param key
     *            specified key
     * @param start
     *            specified member
     * @param end
     *            specified member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */
    @Override
    public Set<Tuple> zrangeWithScores(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrangeWithScores(key, start, end);
            }
        });
    }

    /**
     * zrevrangeWithScores
     * 
     * @param key
     *            specified key
     * @param start
     *            specified member
     * @param end
     *            specified member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */
    @Override
    public Set<Tuple> zrevrangeWithScores(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrevrangeWithScores(key, start, end);
            }
        });
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     *            specified key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    @Override
    public Long zcard(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     *            specified key
     * @param member
     *            specified member
     * @return the score
     */
    @Override
    public Double zscore(final String bizkey, final String nameSpace, final String member) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Double>() {
            public Double invoke(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }

    /**
     * Sort a Set or a List.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key.
     * By default sorting is numeric with elements being compared as double
     * precision floating point numbers. This is the simplest form of SORT.
     * 
     * @see #sort(String, String)
     * @see #sort(String, SortingParams)
     * @see #sort(String, SortingParams, String)
     * 
     * 
     * @param key
     *            specified key
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */
    @Override
    public List<String> sort(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.sort(key);
            }
        });
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters.
     * <p>
     * <b>examples:</b>
     * <p>
     * Given are the following sets and key/values:
     * 
     * <pre>
     * x = [1, 2, 3]
     * y = [a, b, c]
     * 
     * k1 = z
     * k2 = y
     * k3 = x
     * 
     * w1 = 9
     * w2 = 8
     * w3 = 7
     * </pre>
     * 
     * Sort Order:
     * 
     * <pre>
     * sort(x) or sort(x, sp.asc())
     * -> [1, 2, 3]
     * 
     * sort(x, sp.desc())
     * -> [3, 2, 1]
     * 
     * sort(y)
     * -> [c, a, b]
     * 
     * sort(y, sp.alpha())
     * -> [a, b, c]
     * 
     * sort(y, sp.alpha().desc())
     * -> [c, a, b]
     * </pre>
     * 
     * Limit (e.g. for Pagination):
     * 
     * <pre>
     * sort(x, sp.limit(0, 2))
     * -> [1, 2]
     * 
     * sort(y, sp.alpha().desc().limit(1, 2))
     * -> [b, a]
     * </pre>
     * 
     * Sorting by external keys:
     * 
     * <pre>
     * sort(x, sb.by(w*))
     * -> [3, 2, 1]
     * 
     * sort(x, sb.by(w*).desc())
     * -> [1, 2, 3]
     * </pre>
     * 
     * Getting external keys:
     * 
     * <pre>
     * sort(x, sp.by(w*).get(k*))
     * -> [x, y, z]
     * 
     * sort(x, sp.by(w*).get(#).get(k*))
     * -> [3, x, 2, y, 1, z]
     * </pre>
     * 
     * @see #sort(String)
     * @see #sort(String, SortingParams, String)
     * 
     * @param key
     *            specified key
     * @param sortingParameters
     *            sortingParameters
     * @return a list of sorted elements.
     */
    @Override
    public List<String> sort(final String bizkey, final String nameSpace, final SortingParams sortingParameters) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.sort(key, sortingParameters);
            }
        });
    }

    /**
     * zcount
     * 
     * @param key
     *            specified key
     * @param min
     *            min value
     * @param max
     *            max value
     * @return zount
     */
    @Override
    public Long zcount(final String bizkey, final String nameSpace, final double min, final double max) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, String, String)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     *            specified key
     * @param min
     *            min value
     * @param max
     *            max value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<String> zrangeByScore(final String bizkey, final String nameSpace, final double min, final double max) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }

    /**
     * zrevrangeByScore
     * 
     * @param key
     *            specified key
     * @param max
     *            max value
     * @param min
     *            min value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */
    @Override
    public Set<String> zrevrangeByScore(final String bizkey, final String nameSpace, final double max, final double min) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     *            specified key
     * @param min
     *            min value
     * @param max
     *            max value
     * @param offset
     *            offset value
     * @param count
     *            count number
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<String> zrangeByScore(final String bizkey, final String nameSpace, final double min, final double max,
            final int offset, final int count) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max, offset, count);
            }
        });
    }

    /**
     * zrevrangeByScore
     * 
     * @param key
     *            specified key
     * @param max
     *            max value
     * @param min
     *            min value
     * @param offset
     *            offset value
     * @param count
     *            count value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */
    @Override
    public Set<String> zrevrangeByScore(final String bizkey, final String nameSpace, final double max,
            final double min, final int offset, final int count) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min, offset, count);
            }
        });
    }

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     * 
     * @param key
     *            specified key
     * @param start
     *            start rank
     * @param end
     *            end rank
     * @return range
     */
    @Override
    public Long zremrangeByRank(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param key
     *            specified key
     * @param start
     *            start positon
     * @param end
     *            end positon
     * @return Integer reply, specifically the number of elements removed.
     */
    @Override
    public Long zremrangeByScore(final String bizkey, final String nameSpace, final double start, final double end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     *            specified key
     * @param min
     *            min value
     * @param max
     *            max value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String bizkey, final String nameSpace, final double min,
            final double max) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }

    /**
     * zrevrangeByScoreWithScores
     * 
     * @param key
     *            specified key
     * @param max
     *            max value
     * @param min
     *            min value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String bizkey, final String nameSpace, final double max,
            final double min) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     *            specified key
     * @param min
     *            min value
     * @param max
     *            max value
     * @param offset
     *            offset
     * @param count
     *            count
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<Tuple> zrangeByScoreWithScores(final String bizkey, final String nameSpace, final double min,
            final double max, final int offset, final int count) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
            }
        });
    }

    /**
     * zrevrangeByScoreWithScores
     * 
     * @param key
     *            specified key
     * @param max
     *            max value
     * @param min
     *            min value
     * @param offset
     *            offset value
     * @param count
     *            count number
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(final String bizkey, final String nameSpace, final double max,
            final double min, final int offset, final int count) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<Tuple>>() {
            public Set<Tuple> invoke(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
    }

    /**
     * linsert
     * 
     * @param key
     *            specified key
     * @param where
     *            list position
     * @param pivot
     *            pivot
     * @param value
     *            value
     * @return result
     */
    @Override
    public Long linsert(final String bizkey, final String nameSpace, final LIST_POSITION where, final String pivot,
            final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.linsert(key, where, pivot, value);
            }
        });
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    @Override
    public Long del(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.del(key);
            }
        });
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#rpush(String, String)
     * 
     * @param key
     *            specified key
     * @param fields
     *            string fields
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    @Override
    public Long lpush(final String bizkey, final String nameSpace, final String... fields) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                long r = 0;
                for (String field : fields) {
                    r = r + jedis.lpush(key, field);
                }
                return r;
            }
        });
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#lpush(String, String)
     * 
     * @param key
     *            specified key
     * @param fields
     *            string fields
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    @Override
    public Long rpush(final String bizkey, final String nameSpace, final String... fields) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                long r = 0;
                for (String field : fields) {
                    r = r + jedis.rpush(key, field);
                }
                return r;
            }
        });
    }

    /**
     * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。当 key
     * 不存在时，自动生成一个新的字符串值。 字符串会进行伸展(grown)以确保它可以将 value
     * 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。 offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在
     * 512 MB 之内)。
     * 
     * @param key
     *            所储存的字符串值
     * @param offset
     *            偏移量
     * @param value
     *            参数
     * @return 结果
     */
    @Override
    public Boolean setbit(final String bizkey, final String nameSpace, final long offset, final String value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Boolean>() {
            public Boolean invoke(Jedis jedis) {
                return jedis.setbit(key, offset, value);
            }
        });
    }

    /**
     * 返回 key 所储存的字符串值的长度。当 key 储存的不是字符串值时，返回一个错误。
     * 
     * @param key
     *            所储存的字符串值
     * @return 长度
     */
    @Override
    public Long strlen(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.strlen(key);
            }
        });
    }

    /**
     * 打印一个特定的信息 message ，测试时使用。
     * 
     * @param string
     *            特定的信息
     * @return message
     */
    @Override
    public String echo(final String string) {
        return this.performFunction("", new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.echo(string);
            }
        });
    }

    /**
     * 计算给定字符串中，被设置为 1 的比特位的数量。一般情况下，给定的整个字符串都会被进行计数，
     * 
     * @param key
     *            给定的字符串
     * @return 比特位的数量
     */
    @Override
    public Long bitcount(final String bizkey, final String nameSpace) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.bitcount(key);
            }
        });
    }

    /**
     * 计算给定字符串中，被设置为 1 的比特位的数量。一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end
     * 参数，可以让计数只在特定的位上进行。 start 和 end 参数的设置和 GETRANGE 命令类似，都可以使用负数值：比如 -1
     * 表示最后一个位，而 -2 表示倒数第二个位，以此类推。 不存在的 key 被当成是空字符串来处理，因此对一个不存在的 key 进行
     * BITCOUNT 操作，结果为 0 。
     * 
     * @param key
     *            给定的字符串
     * @param start
     *            开始位置
     * @param end
     *            结束位置
     * @return 比特位的数量
     */
    @Override
    public Long bitcount(final String bizkey, final String nameSpace, final long start, final long end) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.bitcount(key, start, end);
            }
        });
    }

    /**
     * 根据key和集合类型设置缓存对象
     * 
     * @param key
     *            键
     * @param value
     *            值
     * @param <T>
     *            泛型对象
     */
    @Override
    public <T> String set(final String bizkey, final String nameSpace, final T value, final int time) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return set(bizkey, nameSpace, CacheUtils.toJSONString(key, value), time);
    }

    /**
     * 根据key和集合类型设置缓存对象
     * 
     * @param key
     *            键
     * @param value
     *            值
     * @param <T>
     *            泛型对象
     */
    @Override
    public <T> String setex(final String bizkey, final String nameSpace, final int time, final T value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return setex(bizkey, nameSpace, time, CacheUtils.toJSONString(key, value));
    }

    /**
     * 根据key和类的类别获取缓存对象
     * 
     * @param key
     *            键
     * @param value
     *            值
     * @param <T>
     *            泛型对象
     * @return json字符串
     */
    @Override
    public <T> T get(final String bizkey, final String nameSpace, Class<T> value, final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        String res = get(bizkey, nameSpace, null);
        T rtn = null;
        if (StringUtils.isNotEmpty(res)) {
            rtn = CacheUtils.parseObject(key, res, value);
        } else {
            if (gbs != null) {
                rtn = gbs.invoke();
                // 取出的数据要set回去
                if (null != rtn) {
                    set(bizkey, nameSpace, rtn, gbs.getExpiredTime());
                }
            }
        }
        return rtn;
    }

    @Override
    public <T> T get(final String bizkey, final String nameSpace, TypeReference<T> type, final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        String res = get(bizkey, nameSpace, null);
        T rtn = null;
        if (StringUtils.isNotEmpty(res)) {
            rtn = CacheUtils.parseObject(key, res, type);
        } else {
            if (gbs != null) {
                rtn = gbs.invoke();
                // 取出的数据要set回去
                if (null != rtn) {
                    set(bizkey, nameSpace, rtn, gbs.getExpiredTime());
                }
            }
        }
        return rtn;
    }

    @Override
    public String mset(final Map<String, String> bizkeyValues, final String nameSpace) {
        return this.performFunction("", new CallBack<String>() {
            public String invoke(Jedis jedis) {
                return jedis.mset(CacheUtils.smapToArray(bizkeyValues, nameSpace));
            }
        });
    }

    @Override
    public List<String> mget(String[] bizkeys, String nameSpace) {
        final String[] key = CacheUtils.getKeyByNamespace(bizkeys, nameSpace);
        return this.performFunction("", new CallBack<List<String>>() {
            public List<String> invoke(Jedis jedis) {
                return jedis.mget(key);
            }
        });
    }

    @Override
    public <T> Long hsetObject(String bizkey, String nameSpace, String field, T value) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return hset(bizkey, nameSpace, field, CacheUtils.toJSONString(key, value));
    }

    @Override
    public <T> T hgetObject(final String bizkey, final String nameSpace, final String field, Class<T> value,
            final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        String res = hget(bizkey, nameSpace, field, null);
        T rtn = null;
        if (StringUtils.isNotEmpty(res)) {
            rtn = CacheUtils.parseObject(key, res, value);
        } else {
            if (gbs != null) {
                rtn = gbs.invoke();
            }
            if (null != rtn) {
                hsetObject(bizkey, nameSpace, field, rtn);
            }
        }
        return rtn;
    }

    @Override
    public <T> T hgetObject(final String bizkey, final String nameSpace, final String field, TypeReference<T> type,
            final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        String res = hget(bizkey, nameSpace, field, null);
        T rtn = null;
        if (StringUtils.isNotEmpty(res)) {
            rtn = CacheUtils.parseObject(key, res, type);
        } else {
            if (gbs != null) {
                rtn = gbs.invoke();
            }
            if (null != rtn) {
                hsetObject(bizkey, nameSpace, field, rtn);
            }
        }
        return rtn;
    }

    @Override
    public void hdelObject(String bizkey, String nameSpace, String... field) {
        hdel(bizkey, nameSpace, field);
    }

    @Override
    public <T> Map<String, T> hgetAllObjects(final String bizkey, final String nameSpace, final TypeReference<T> type,
            final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Map<String, T>>() {
            public Map<String, T> invoke(Jedis jedis) {
                try {
                    Map<String, String> all = jedis.hgetAll(key);
                    Map<String, T> allObjs = new HashMap<String, T>();
                    for (Entry<String, String> item : all.entrySet()) {
                        String _key = item.getKey();
                        T _value = CacheUtils.parseObject(key, item.getValue(), type);
                        allObjs.put(_key, _value);
                    }
                    return allObjs;
                } catch (Exception e) {
                    logger.error("key:" + key + "hgetAllObjects Exception：" + e.getMessage());
                }
                return null;
            }
        });
    }

    @Override
    public <T> Map<String, T> hgetAllObjects(final String bizkey, final String nameSpace, final Class<T> value,
            final GetDataCallBack<T> gbs) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Map<String, T>>() {
            public Map<String, T> invoke(Jedis jedis) {
                try {
                    Map<String, String> all = jedis.hgetAll(key);
                    Map<String, T> allObjs = new HashMap<String, T>();
                    for (Entry<String, String> item : all.entrySet()) {
                        String _key = item.getKey();
                        T _value = CacheUtils.parseObject(key, item.getValue(), value);
                        allObjs.put(_key, _value);
                    }
                    return allObjs;
                } catch (Exception e) {
                    logger.error("key:" + key + "hgetAllObjects Exception：" + e.getMessage());
                }
                return null;
            }
        });
    }

    @Override
    public Long del(String[] bizkeys, String nameSpace) {
        final String[] keys = CacheUtils.getKeyByNamespace(bizkeys, nameSpace);
        return this.performFunction("", new CallBack<Long>() {
            public Long invoke(Jedis jedis) {
                return jedis.del(keys);
            }
        });
    }

    @Override
    public <T> List<T> hvalsObject(final String bizkey, final String nameSpace, final TypeReference<T> type) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        List<String> res = hvals(bizkey, nameSpace);
        List<T> rtnList = null;
        T rtn = null;
        if (null != res && res.size() > 0) {
            rtnList = new ArrayList<T>();
            for (String tmp : res) {
                rtn = CacheUtils.parseObject(key, tmp, type);
                rtnList.add(rtn);
            }
        }
        return rtnList;
    }

    @Override
    public <T> List<T> hvalsObject(String bizkey, String nameSpace, Class<T> type) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        List<String> res = hvals(bizkey, nameSpace);
        List<T> rtnList = null;
        T rtn = null;
        if (null != res && res.size() > 0) {
            rtnList = new ArrayList<T>();
            for (String tmp : res) {
                rtn = CacheUtils.parseObject(key, tmp, type);
                rtnList.add(rtn);
            }
        }
        return rtnList;
    }

    @Override
    public List<Map.Entry<String, String>> hscan(final String bizkey, final String nameSpace, final String match) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<List<Map.Entry<String, String>>>() {
            public List<Map.Entry<String, String>> invoke(Jedis jedis) {
                try {
                    int cursor = 0;
                    ScanParams scanParams = new ScanParams();
                    scanParams.match(match);
                    ScanResult<Map.Entry<String, String>> scanResult;
                    List<Map.Entry<String, String>> res = new ArrayList<Map.Entry<String, String>>();
                    do {
                        scanResult = jedis.hscan(key, String.valueOf(cursor), scanParams);
                        res.addAll(scanResult.getResult());
                        cursor = Integer.parseInt(scanResult.getStringCursor());
                    } while (cursor > 0);
                    return res;
                } catch (Exception ex) {
                    logger.error("hscan key:" + key + ",match:" + match + ",error:", ex);
                }
                return null;
            }
        });
    }

    @Override
    public Set<String> sscan(final String bizkey, final String nameSpace, final String match) {
        final String key = CacheUtils.getKeyByNamespace(bizkey, nameSpace);
        return this.performFunction(key, new CallBack<Set<String>>() {
            public Set<String> invoke(Jedis jedis) {
                try {
                    int cursor = 0;
                    ScanParams scanParams = new ScanParams();
                    scanParams.match(match);
                    ScanResult<String> scanResult;
                    Set<String> res = new HashSet<String>();
                    do {
                        scanResult = jedis.sscan(key, String.valueOf(cursor), scanParams);
                        res.addAll(scanResult.getResult());
                        cursor = Integer.parseInt(scanResult.getStringCursor());
                    } while (cursor > 0);
                    return res;
                } catch (Exception ex) {
                    logger.error("sscan key:" + key + ",match:" + match + ",error:", ex);
                }
                return null;
            }
        });
    }

    @Override
    public Object patternDel(final String pattern, final String nameSpace) {
        final String patternKey = CacheUtils.getKeyByNamespace(pattern, nameSpace);
        // final String script =
        // "return redis.call('del', unpack(redis.call('keys','"+patternKey+"')))";
        final String script = "local keys = redis.call('keys', '"
                + patternKey
                + "') \n for i=1,#keys,2000 do \n redis.call('del', unpack(keys, i, math.min(i+1999, #keys))) \n end \n return keys";
        return this.performFunction("", new CallBack<Object>() {
            public Object invoke(Jedis jedis) {
                try {
                    return jedis.eval(script);
                } catch (Exception e) {
                    logger.error("patternDel:" + patternKey + " eval Exception：" + e.getMessage());
                }
                return null;
            }
        });
    }

    @Override
    public Object eval(final String script, final List<String> bizkeys, final String nameSpace, final List<String> args) {
        final List<String> keys = CacheUtils.getKeyByNamespace(bizkeys, nameSpace);
        return this.performFunction("", new CallBack<Object>() {
            public Object invoke(Jedis jedis) {
                return jedis.eval(script, keys, args);
            }
        });
    }

    @Override
    public Pipeline pipelined() {
        return this.performFunction("", new CallBack<Pipeline>() {
            public Pipeline invoke(Jedis jedis) {
                return jedis.pipelined();
            }
        });
    }
}
