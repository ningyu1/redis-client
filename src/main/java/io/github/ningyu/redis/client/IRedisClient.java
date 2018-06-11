/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: IRedisClient.java
 * Author:   ningyu
 * Date:     2017年4月24日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package io.github.ningyu.redis.client;

import io.github.ningyu.redis.callback.GetDataCallBack;

import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.BinaryClient.LIST_POSITION;

import com.alibaba.fastjson.TypeReference;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年4月24日 上午10:29:26
 */
/**
 * @author ningyu
 * 
 */
public interface IRedisClient {

    /**
     * @param key
     *            主键
     * @param nameSpace
     *            命名空间
     * @param value
     *            值
     * @param expire
     *            seconds, -1表示无时间限制
     * @return Status code reply
     */

    public String set(final String bizkey, final String nameSpace, final String value, final int expire);

    /**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * 
     * @param bizkey
     * @param nameSpace
     * @param value
     * @param nxxx
     *            NX|XX, NX -- Only set the key if it does not already exist. XX
     *            -- Only set the key if it already exist.
     * @param expx
     *            EX|PX, expire time units: EX = seconds; PX = milliseconds
     * @param expire
     *            expire time in the units of {@param #expx}
     * @return Status code reply
     */

    public String set(final String bizkey, final String nameSpace, final String value, final String nxxx,
            final String expx, final long expire);

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public String get(final String bizkey, final String nameSpace, final GetDataCallBack<String> gbs);

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "1" is returned. Note that even keys set with an empty
     * string as value will return "0".
     * 
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Boolean reply, true if the key exists, otherwise false
     */

    public Boolean exists(final String bizkey, final String nameSpace);

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */

    public String type(final String bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param seconds
     *            associated timeout
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */

    public Long expire(final String bizkey, final String nameSpace, final int seconds);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param unixTime
     *            UNIX timestamp
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */

    public Long expireAt(final String bizkey, final String nameSpace, final long unixTime);

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(String, int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */

    public Long ttl(final String bizkey, final String nameSpace);

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param offset
     *            offset
     * @param value
     *            string value
     * @return result
     */

    public Boolean setbit(final String bizkey, final String nameSpace, final long offset, final boolean value);

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param offset
     *            offset
     * @return result
     */

    public Boolean getbit(final String bizkey, final String nameSpace, final long offset);

    /**
     * setrange
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param offset
     *            offset
     * @param value
     *            string value
     * @return result
     * 
     */

    public Long setrange(final String bizkey, final String nameSpace, final long offset, final String value);

    /**
     * setrange
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param startOffset
     *            start offset
     * @param endOffset
     *            end Offset
     * @return result
     * 
     */

    public String getrange(final String bizkey, final String nameSpace, final long startOffset, final long endOffset);

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param value
     *            the old value
     * @return Bulk reply
     */

    public String getSet(final String bizkey, final String nameSpace, final String value);

    /**
     * SETNX works exactly like {@link #set(String, String) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param value
     *            value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */

    public Long setnx(final String bizkey, final String nameSpace, final String value);

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param seconds
     *            timeout seconds
     * @param <T>
     *            value
     * @return Bulk reply
     */

    public <T> String setex(final String bizkey, final String nameSpace, final int seconds, final T value);

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param seconds
     *            timeout seconds
     * @param value
     *            value
     * @return Bulk reply
     */

    public String setex(final String bizkey, final String nameSpace, final int seconds, final String value);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param integer
     *            integer value
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */

    public Long decrBy(final String bizkey, final String nameSpace, final long integer);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */

    public Long decr(final String bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param integer
     *            integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */

    public Long incrBy(final String bizkey, final String nameSpace, final long integer);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */

    public Long incr(final String bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            start
     * @param end
     *            end
     * @return Bulk reply
     */

    public String substr(final String bizkey, final String nameSpace, final int start, final int end);

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            hash field
     * @param value
     *            specified value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */

    public Long hset(final String bizkey, final String nameSpace, final String field, final String value);

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            hash field
     * @return Bulk reply
     */

    public String hget(final String bizkey, final String nameSpace, final String field,
            final GetDataCallBack<String> gbs);

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            hash field
     * @param value
     *            specified value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */

    public Long hsetnx(final String bizkey, final String nameSpace, final String field, final String value);

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param hash
     *            hash
     * @return Return OK or Exception if hash is empty
     */

    public String hmset(final String bizkey, final String nameSpace, final Map<String, String> hash);

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param fields
     *            specified fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */

    public List<String> hmget(final String bizkey, final String nameSpace, final String... fields);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            specified key
     * @param value
     *            specified value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */

    public Long hincrBy(final String bizkey, final String nameSpace, final String field, final long value);

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            specified key
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */

    public Boolean hexists(final String bizkey, final String nameSpace, final String field);

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param field
     *            specified field
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */

    public Long hdel(final String bizkey, final String nameSpace, final String... field);

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */

    public Long hlen(final String bizkey, final String nameSpace);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return result
     */

    public Set<String> hkeys(final String bizkey, final String nameSpace);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return result
     */

    public List<String> hvals(final String bizkey, final String nameSpace);

    /**
     * @param <T>
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param type
     * @return result
     */

    public <T> List<T> hvalsObject(final String bizkey, final String nameSpace, TypeReference<T> type);

    /**
     * @param <T>
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param type
     * @return result
     */

    public <T> List<T> hvalsObject(final String bizkey, final String nameSpace, Class<T> type);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return result
     */

    public Map<String, String> hgetAll(final String bizkey, final String nameSpace);

    /**
     * 获取长度
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return 长度
     */

    public Long llen(final String bizkey, final String nameSpace);

    /**
     * 根据键值，起始位置获取结果列表
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            开始位置
     * @param end
     *            结束为止
     * @return 结果列表
     */

    public List<String> lrange(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * 根据键值，起始位置trim字符串
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            开始位置
     * @param end
     *            结束位置
     * @return trim后的字符串
     */

    public String ltrim(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * 获取指定索引的字符串
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param index
     *            索引
     * @return 指定索引的字符串
     */

    public String lindex(final String bizkey, final String nameSpace, final long index);

    /**
     * 设置指定索引值
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param index
     *            索引
     * @param value
     *            值
     * @return result
     */

    public String lset(final String bizkey, final String nameSpace, final long index, final String value);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param count
     *            removed counts
     * @param value
     *            value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */

    public Long lrem(final String bizkey, final String nameSpace, final long count, final String value);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public String lpop(final String bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public String rpop(final String bizkey, final String nameSpace);

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */

    public Long sadd(final String bizkey, final String nameSpace, final String... member);

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Multi bulk reply
     */

    public Set<String> smembers(final String bizkey, final String nameSpace);

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */

    public Long srem(final String bizkey, final String nameSpace, final String... member);

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public String spop(final String bizkey, final String nameSpace);

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public Long scard(final String bizkey, final String nameSpace);

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */

    public Boolean sismember(final String bizkey, final String nameSpace, final String member);

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Bulk reply
     */

    public String srandmember(final String bizkey, final String nameSpace);

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @parm count
     * @return Bulk reply
     */
    public List<String> srandmember(String bizkey, String nameSpace, int count);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param score
     *            specifeid score
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */

    public Long zadd(final String bizkey, final String nameSpace, final double score, final String member);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            specified start member
     * @param end
     *            specified end member
     * @return result
     */

    public Set<String> zrange(final String bizkey, final String nameSpace, final long start, final long end);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */

    public Long zrem(final String bizkey, final String nameSpace, final String... member);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param score
     *            specified score
     * @param member
     *            specified member
     * @return The new score
     */

    public Double zincrby(final String bizkey, final String nameSpace, final double score, final String member);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */

    public Long zrank(final String bizkey, final String nameSpace, final String member);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */

    public Long zrevrank(final String bizkey, final String nameSpace, final String member);

    /**
     * zrevrange
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            specified start member
     * @param end
     *            specified end member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */

    public Set<String> zrevrange(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * zrangeWithScores
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            specified member
     * @param end
     *            specified member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */

    public Set<Tuple> zrangeWithScores(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * zrevrangeWithScores
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            specified member
     * @param end
     *            specified member
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */

    public Set<Tuple> zrevrangeWithScores(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return the cardinality (number of elements) of the set as an integer.
     */

    public Long zcard(final String bizkey, final String nameSpace);

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param member
     *            specified member
     * @return the score
     */

    public Double zscore(final String bizkey, final String nameSpace, final String member);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */

    public List<String> sort(final String bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param sortingParameters
     *            sortingParameters
     * @return a list of sorted elements.
     */

    public List<String> sort(final String bizkey, final String nameSpace, final SortingParams sortingParameters);

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

    public Long zcount(final String bizkey, final String nameSpace, final double min, final double max);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param min
     *            min value
     * @param max
     *            max value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */

    public Set<String> zrangeByScore(final String bizkey, final String nameSpace, final double min, final double max);

    /**
     * zrevrangeByScore
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param max
     *            max value
     * @param min
     *            min value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         range.
     */

    public Set<String> zrevrangeByScore(final String bizkey, final String nameSpace, final double max, final double min);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
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

    public Set<String> zrangeByScore(final String bizkey, final String nameSpace, final double min, final double max,
            final int offset, final int count);

    /**
     * zrevrangeByScore
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
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

    public Set<String> zrevrangeByScore(final String bizkey, final String nameSpace, final double max,
            final double min, final int offset, final int count);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            start rank
     * @param end
     *            end rank
     * @return range
     */

    public Long zremrangeByRank(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            start positon
     * @param end
     *            end positon
     * @return Integer reply, specifically the number of elements removed.
     */

    public Long zremrangeByScore(final String bizkey, final String nameSpace, final double start, final double end);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param min
     *            min value
     * @param max
     *            max value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */

    public Set<Tuple> zrangeByScoreWithScores(final String bizkey, final String nameSpace, final double min,
            final double max);

    /**
     * zrevrangeByScoreWithScores
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param max
     *            max value
     * @param min
     *            min value
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */

    public Set<Tuple> zrevrangeByScoreWithScores(final String bizkey, final String nameSpace, final double max,
            final double min);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
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

    public Set<Tuple> zrangeByScoreWithScores(final String bizkey, final String nameSpace, final double min,
            final double max, final int offset, final int count);

    /**
     * zrevrangeByScoreWithScores
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
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

    public Set<Tuple> zrevrangeByScoreWithScores(final String bizkey, final String nameSpace, final double max,
            final double min, final int offset, final int count);

    /**
     * linsert
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param where
     *            list position
     * @param pivot
     *            pivot
     * @param value
     *            value
     * @return result
     */

    public Long linsert(final String bizkey, final String nameSpace, final LIST_POSITION where, final String pivot,
            final String value);

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */

    public Long del(final String bizkey, final String nameSpace);

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */

    public Long del(final String[] bizkey, final String nameSpace);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param fields
     *            string fields
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */

    public Long lpush(final String bizkey, final String nameSpace, final String... fields);

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
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param fields
     *            string fields
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */

    public Long rpush(final String bizkey, final String nameSpace, final String... fields);

    /**
     * 功能描述： 批量set数据
     * 
     * @param keyValues
     *            键值对
     * @param nameSpace
     *            命名空间
     * 
     * @return set成功与否的状态码
     */
    String mset(Map<String, String> keyValues, String nameSpace);

    /**
     * 功能描述： 批量获取数据
     * 
     * @param keys
     *            指定的key
     * @param nameSpace
     *            命名空间
     * 
     * @return 数据集合
     */
    List<String> mget(String[] keys, String nameSpace);

    /**
     * 对 key 所储存的字符串值，设置或清除指定偏移量上的位(bit)。位的设置或清除取决于 value 参数，可以是 0 也可以是 1 。当 key
     * 不存在时，自动生成一个新的字符串值。 字符串会进行伸展(grown)以确保它可以将 value
     * 保存在指定的偏移量上。当字符串值进行伸展时，空白位置以 0 填充。 offset 参数必须大于或等于 0 ，小于 2^32 (bit 映射被限制在
     * 512 MB 之内)。
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param offset
     *            偏移量
     * @param value
     *            参数
     * @return 结果
     */

    public Boolean setbit(final String bizkey, final String nameSpace, final long offset, final String value);

    /**
     * 返回 key 所储存的字符串值的长度。当 key 储存的不是字符串值时，返回一个错误。
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return 长度
     */

    public Long strlen(final String bizkey, final String nameSpace);

    /**
     * 打印一个特定的信息 message ，测试时使用。
     * 
     * @param string
     *            特定的信息
     * @return message
     */

    public String echo(final String string);

    /**
     * 计算给定字符串中，被设置为 1 的比特位的数量。一般情况下，给定的整个字符串都会被进行计数，
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @return 比特位的数量
     */
    public Long bitcount(final String bizkey, final String nameSpace);

    /**
     * 计算给定字符串中，被设置为 1 的比特位的数量。一般情况下，给定的整个字符串都会被进行计数，通过指定额外的 start 或 end
     * 参数，可以让计数只在特定的位上进行。 start 和 end 参数的设置和 GETRANGE 命令类似，都可以使用负数值：比如 -1
     * 表示最后一个位，而 -2 表示倒数第二个位，以此类推。 不存在的 key 被当成是空字符串来处理，因此对一个不存在的 key 进行
     * BITCOUNT 操作，结果为 0 。
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param start
     *            开始位置
     * @param end
     *            结束位置
     * @return 比特位的数量
     */
    public Long bitcount(final String bizkey, final String nameSpace, final long start, final long end);

    /**
     * 根据key和集合类型设置缓存对象
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param value
     *            值
     * @param <T>
     *            泛型对象
     * @param time
     *            seconds
     */
    public <T> String set(final String bizkey, final String nameSpace, final T value, final int time);

    /**
     * 根据key和类的类别获取缓存对象
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param value
     *            值
     * @param <T>
     *            泛型对象
     * @return <T> 泛型对象
     */
    public <T> T get(final String bizkey, final String nameSpace, Class<T> value, final GetDataCallBack<T> gbs);

    /**
     * 根据key和类的类别获取缓存对象
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param value
     *            值
     * @param TypeReference
     *            <T>
     * @return <T> 泛型对象
     */
    public <T> T get(final String bizkey, final String nameSpace, TypeReference<T> type, final GetDataCallBack<T> gbs);

    /**
     * 使用方法同hset，增加序列化功能（使用JSON）
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param <T>
     *            泛型对象
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    public <T> Long hsetObject(final String bizkey, final String nameSpace, final String field, final T value);

    /**
     * 使用方法同hget
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param <T>
     *            泛型对象
     * @return <T> 泛型对象
     */
    public <T> T hgetObject(final String bizkey, final String nameSpace, final String field, Class<T> value,
            final GetDataCallBack<T> gbs);

    /**
     * 使用方法同hget
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param TypeReference
     *            <T> type
     * @return <T> 泛型对象
     */
    public <T> T hgetObject(final String bizkey, final String nameSpace, final String field, TypeReference<T> type,
            final GetDataCallBack<T> gbs);

    /**
     * 使用方法同hdel
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param <T>
     *            泛型对象
     */
    public void hdelObject(final String bizkey, final String nameSpace, final String... field);

    /**
     * 使用方法同hgetAll
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param <T>
     *            泛型对象
     */
    public <T> Map<String, T> hgetAllObjects(final String bizkey, final String nameSpace, Class<T> value,
            final GetDataCallBack<T> gbs);

    /**
     * 使用方法同hgetAll
     * 
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @param filed
     * @param TypeReference
     *            <T> type
     */
    public <T> Map<String, T> hgetAllObjects(final String bizkey, final String nameSpace, TypeReference<T> type,
            final GetDataCallBack<T> gbs);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @Param cursor
     * */
    public List<Map.Entry<String, String>> hscan(final String bizkey, final String nameSpace, final String match);

    /**
     * @param bizkey
     *            specified key
     * @param nameSpace
     * @Param cursor
     * */
    public Set<String> sscan(final String bizkey, final String nameSpace, final String match);

    /**
     * 按模式批量删除
     * 
     * @param script
     * */
    public Object patternDel(final String pattern, final String nameSpace);

    /**
     * 部分支持eval，需要自己确保所有keys在同一服务器上
     * */
    public Object eval(final String script, final List<String> keys, final String nameSpace, final List<String> args);

    /**
     * pipline模式
     * */
    public Pipeline pipelined();
}
