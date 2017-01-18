/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: RedisClient.java
 * Author:   ningyu
 * Date:     2017年1月16日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.client;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.tsoft.framework.redis.callback.CallBack;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年1月16日 下午2:54:36
 */
public class RedisClientImpl extends BaseClient implements RedisClient {
	
	private final Logger logger = LoggerFactory.getLogger(RedisClientImpl.class.getName());
	
	/**
	   * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
	   * GB).
	   * <p>
	   * Time complexity: O(1)
	   * @param key
	   * @param value
	   * @return Status code reply
	   */
	@Override
	public String set(final String key, final String value) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.set(key, value);
			}
		});
	}

	/**
	   * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
	   * GB).
	   * @param key
	   * @param value
	   * @param nxxx NX|XX, NX -- Only set the key if it does not already exist. XX -- Only set the key
	   *          if it already exist.
	   * @param expx EX|PX, expire time units: EX = seconds; PX = milliseconds
	   * @param time expire time in the units of <code>expx</code>
	   * @return Status code reply
	   */
	@Override
	public String set(final String key, final String value, final String nxxx, final String expx,
			final long time) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.set(key, value, nxxx, expx, time);
			}
		});
	}

	/**
	   * Set the string value as value of the key. The string can't be longer than 1073741824 bytes (1
	   * GB).
	   * @param key
	   * @param value
	   * @param nxxx NX|XX, NX -- Only set the key if it does not already exist. XX -- Only set the key
	   *          if it already exist.
	   * @return Status code reply
	   */
	@Override
	public String set(final String key, final String value, final String nxxx) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.set(key, value, nxxx);
			}
		});
	}

	/**
	   * Get the value of the specified key. If the key does not exist null is returned. If the value
	   * stored at key is not a string an error is returned because GET can only handle string values.
	   * <p>
	   * Time complexity: O(1)
	   * @param key
	   * @return Bulk reply
	   */
	@Override
	public String get(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}

	/**
	   * Test if the specified key exists. The command returns "1" if the key exists, otherwise "0" is
	   * returned. Note that even keys set with an empty string as value will return "1". Time
	   * complexity: O(1)
	   * @param key
	   * @return Boolean reply, true if the key exists, otherwise false
	   */
	@Override
	public Boolean exists(final String key) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}

	/**
	   * Undo a {@link #expire(String, int) expire} at turning the expire key into a normal key.
	   * <p>
	   * Time complexity: O(1)
	   * @param key
	   * @return Integer reply, specifically: 1: the key is now persist. 0: the key is not persist (only
	   *         happens when key not set).
	   */
	@Override
	public Long persist(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.persist(key);
			}
		});
	}

	/**
	   * Return the type of the value stored at key in form of a string. The type can be one of "none",
	   * "string", "list", "set". "none" is returned if the key does not exist. Time complexity: O(1)
	   * @param key
	   * @return Status code reply, specifically: "none" if the key does not exist "string" if the key
	   *         contains a String value "list" if the key contains a List value "set" if the key
	   *         contains a Set value "zset" if the key contains a Sorted Set value "hash" if the key
	   *         contains a Hash value
	   */
	@Override
	public String type(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.type(key);
			}
		});
	}

	/**
	   * Set a timeout on the specified key. After the timeout the key will be automatically deleted by
	   * the server. A key with an associated timeout is said to be volatile in Redis terminology.
	   * <p>
	   * Voltile keys are stored on disk like the other keys, the timeout is persistent too like all the
	   * other aspects of the dataset. Saving a dataset containing expires and stopping the server does
	   * not stop the flow of time as Redis stores on disk the time when the key will no longer be
	   * available as Unix time, and not the remaining seconds.
	   * <p>
	   * Since Redis 2.1.3 you can update the value of the timeout of a key already having an expire
	   * set. It is also possible to undo the expire at all turning the key into a normal key using the
	   * {@link #persist(String) PERSIST} command.
	   * <p>
	   * Time complexity: O(1)
	   * @see <a href="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
	   * @param key
	   * @param seconds
	   * @return Integer reply, specifically: 1: the timeout was set. 0: the timeout was not set since
	   *         the key already has an associated timeout (this may happen only in Redis versions &lt;
	   *         2.1.3, Redis &gt;= 2.1.3 will happily update the timeout), or the key does not exist.
	   */
	@Override
	public Long expire(final String key, final int seconds) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.expire(key, seconds);
			}
		});
	}

	@Override
	public Long pexpire(final String key, final long milliseconds) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.pexpire(key, milliseconds);
			}
		});
	}

	@Override
	public Long expireAt(final String key, final long unixTime) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.expireAt(key, unixTime);
			}
		});
	}

	@Override
	public Long pexpireAt(final String key, final long millisecondsTimestamp) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.pexpireAt(key, millisecondsTimestamp);
			}
		});
	}

	@Override
	public Long ttl(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.ttl(key);
			}
		});
	}

	@Override
	public Long pttl(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.pttl(key);
			}
		});
	}

	@Override
	public Boolean setbit(final String key, final long offset, final boolean value) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.setbit(key, offset, value);
			}
		});
	}

	@Override
	public Boolean setbit(final String key, final long offset, final String value) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.setbit(key, offset, value);
			}
		});
	}

	@Override
	public Boolean getbit(final String key, final long offset) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.getbit(key, offset);
			}
		});
	}

	@Override
	public Long setrange(final String key, final long offset, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.setrange(key, offset, value);
			}
		});
	}

	@Override
	public String getrange(final String key, final long startOffset, final long endOffset) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.getrange(key, startOffset, endOffset);
			}
		});
	}

	@Override
	public String getSet(final String key, final String value) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.getSet(key, value);
			}
		});
	}

	@Override
	public Long setnx(final String key, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.setnx(key, value);
			}
		});
	}

	@Override
	public String setex(final String key, final int seconds, final String value) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.setex(key, seconds, value);
			}
		});
	}

	@Override
	public String psetex(final String key, final long milliseconds, final String value) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.psetex(key, milliseconds, value);
			}
		});
	}

	@Override
	public Long decrBy(final String key, final long integer) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.decrBy(key, integer);
			}
		});
	}

	@Override
	public Long decr(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.decr(key);
			}
		});
	}

	@Override
	public Long incrBy(final String key, final long integer) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.incrBy(key, integer);
			}
		});
	}

	@Override
	public Double incrByFloat(final String key, final double value) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.incrByFloat(key, value);
			}
		});
	}

	@Override
	public Long incr(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.incr(key);
			}
		});
	}

	@Override
	public Long append(final String key, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.append(key, value);
			}
		});
	}

	@Override
	public String substr(final String key, final int start, final int end) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.substr(key, start, end);
			}
		});
	}

	@Override
	public Long hset(final String key, final String field, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.hset(key, field, value);
			}
		});
	}

	@Override
	public String hget(final String key, final String field) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.hget(key, field);
			}
		});
	}

	@Override
	public Long hsetnx(final String key, final String field, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.hsetnx(key, field, value);
			}
		});
	}

	@Override
	public String hmset(final String key, final Map<String, String> hash) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.hmset(key, hash);
			}
		});
	}

	@Override
	public List<String> hmget(final String key, final String... fields) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.hmget(key, fields);
			}
		});
	}

	@Override
	public Long hincrBy(final String key, final String field, final long value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.hincrBy(key, field, value);
			}
		});
	}

	@Override
	public Double hincrByFloat(final String key, final String field, final double value) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.hincrByFloat(key, field, value);
			}
		});
	}

	@Override
	public Boolean hexists(final String key, final String field) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.hexists(key, field);
			}
		});
	}

	@Override
	public Long hdel(final String key, final String... field) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.hdel(key, field);
			}
		});
	}

	@Override
	public Long hlen(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.hlen(key);
			}
		});
	}

	@Override
	public Set<String> hkeys(final String key) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.hkeys(key);
			}
		});
	}

	@Override
	public List<String> hvals(final String key) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.hvals(key);
			}
		});
	}

	@Override
	public Map<String, String> hgetAll(final String key) {
		return executeCache(new CallBack<Map<String, String>>() {
			@Override
			public Map<String, String> invoke(Jedis jedis) {
				return jedis.hgetAll(key);
			}
		});
	}

	@Override
	public Long rpush(final String key, final String... string) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.rpush(key, string);
			}
		});
	}

	@Override
	public Long lpush(final String key, final String... string) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.lpush(key, string);
			}
		});
	}

	@Override
	public Long llen(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.llen(key);
			}
		});
	}

	@Override
	public List<String> lrange(final String key, final long start, final long end) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.lrange(key, start, end);
			}
		});
	}

	@Override
	public String ltrim(final String key, final long start, final long end) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.ltrim(key, start, end);
			}
		});
	}

	@Override
	public String lindex(final String key, final long index) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.lindex(key, index);
			}
		});
	}

	@Override
	public String lset(final String key, final long index, final String value) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.lset(key, index, value);
			}
		});
	}

	@Override
	public Long lrem(final String key, final long count, final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.lrem(key, count, value);
			}
		});
	}

	@Override
	public String lpop(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.lpop(key);
			}
		});
	}

	@Override
	public String rpop(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.rpop(key);
			}
		});
	}

	@Override
	public Long sadd(final String key, final String... member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.sadd(key, member);
			}
		});
	}

	@Override
	public Set<String> smembers(final String key) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.smembers(key);
			}
		});
	}

	@Override
	public Long srem(final String key, final String... member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.srem(key, member);
			}
		});
	}

	@Override
	public String spop(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.spop(key);
			}
		});
	}

	@Override
	public Set<String> spop(final String key, final long count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.spop(key, count);
			}
		});
	}

	@Override
	public Long scard(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.scard(key);
			}
		});
	}

	@Override
	public Boolean sismember(final String key, final String member) {
		return executeCache(new CallBack<Boolean>() {
			@Override
			public Boolean invoke(Jedis jedis) {
				return jedis.sismember(key, member);
			}
		});
	}

	@Override
	public String srandmember(final String key) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.srandmember(key);
			}
		});
	}

	@Override
	public List<String> srandmember(final String key, final int count) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.srandmember(key, count);
			}
		});
	}

	@Override
	public Long strlen(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.strlen(key);
			}
		});
	}

	@Override
	public Long zadd(final String key, final double score, final String member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zadd(key, score, member);
			}
		});
	}

	@Override
	public Long zadd(final String key, final double score, final String member, final ZAddParams params) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zadd(key, score, member, params);
			}
		});
	}

	@Override
	public Long zadd(final String key, final Map<String, Double> scoreMembers) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zadd(key, scoreMembers);
			}
		});
	}

	@Override
	public Long zadd(final String key, final Map<String, Double> scoreMembers,
			final ZAddParams params) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zadd(key, scoreMembers, params);
			}
		});
	}

	@Override
	public Set<String> zrange(final String key, final long start, final long end) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrange(key, start, end);
			}
		});
	}

	@Override
	public Long zrem(final String key, final String... member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zrem(key, member);
			}
		});
	}

	@Override
	public Double zincrby(final String key, final double score, final String member) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.zincrby(key, score, member);
			}
		});
	}

	@Override
	public Double zincrby(final String key, final double score, final String member,
			final ZIncrByParams params) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.zincrby(key, score, member, params);
			}
		});
	}

	@Override
	public Long zrank(final String key, final String member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zrank(key, member);
			}
		});
	}

	@Override
	public Long zrevrank(final String key, final String member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zrevrank(key, member);
			}
		});
	}

	@Override
	public Set<String> zrevrange(final String key, final long start, final long end) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrange(key, start, end);
			}
		});
	}

	@Override
	public Set<Tuple> zrangeWithScores(final String key, final long start, final long end) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrangeWithScores(key, start, end);
			}
		});
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(final String key, final long start, final long end) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrevrangeWithScores(key, start, end);
			}
		});
	}

	@Override
	public Long zcard(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zcard(key);
			}
		});
	}

	@Override
	public Double zscore(final String key, final String member) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.zscore(key, member);
			}
		});
	}

	@Override
	public List<String> sort(final String key) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.sort(key);
			}
		});
	}

	@Override
	public List<String> sort(final String key, final SortingParams sortingParameters) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.sort(key, sortingParameters);
			}
		});
	}

	@Override
	public Long zcount(final String key, final double min, final double max) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}
		});
	}

	@Override
	public Long zcount(final String key, final String min, final String max) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zcount(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrangeByScore(final String key, final double min, final double max) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrangeByScore(final String key, final String min, final String max) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min);
			}
		});
	}

	@Override
	public Set<String> zrangeByScore(final String key, final double min, final double max,
			int offset, int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByScore(final String key, final String max, final String min) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min);
			}
		});
	}

	@Override
	public Set<String> zrangeByScore(final String key, final String min, final String max,
			int offset, int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByScore(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByScore(final String key, final double max, final double min,
			int offset, int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min);
			}
		});
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}
		});
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max,
			final double min) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, max, min);
			}
		});
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(final String key, final double min,
			final double max, final int offset, final int count) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByScore(final String key, final String max, final String min,
			final int offset, final int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByScore(key, max, min, offset, count);
			}
		});
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(final String key, final String min, final String max) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max);
			}
		});
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max,
			final String min) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, max, min);
			}
		});
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(final String key, final String min,
			final String max, final int offset, final int count) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			}
		});
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max,
			final double min, final int offset, final int count) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
			}
		});
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(final String key, final String max,
			final String min, final int offset, final int count) {
		return executeCache(new CallBack<Set<Tuple>>() {
			@Override
			public Set<Tuple> invoke(Jedis jedis) {
				return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
			}
		});
	}

	@Override
	public Long zremrangeByRank(final String key, final long start, final long end) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zremrangeByRank(key, start, end);
			}
		});
	}

	@Override
	public Long zremrangeByScore(final String key, final double start, final double end) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}
		});
	}

	@Override
	public Long zremrangeByScore(final String key, final String start, final String end) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zremrangeByScore(key, start, end);
			}
		});
	}

	@Override
	public Long zlexcount(final String key, final String min, final String max) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zlexcount(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrangeByLex(final String key, final String min, final String max) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max);
			}
		});
	}

	@Override
	public Set<String> zrangeByLex(final String key, final String min, final String max,
			final int offset, final int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrangeByLex(key, min, max, offset, count);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByLex(final String key, final String max, final String min) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByLex(key, max, min);
			}
		});
	}

	@Override
	public Set<String> zrevrangeByLex(final String key, final String max, final String min,
			final int offset, final int count) {
		return executeCache(new CallBack<Set<String>>() {
			@Override
			public Set<String> invoke(Jedis jedis) {
				return jedis.zrevrangeByLex(key, max, min, offset, count);
			}
		});
	}

	@Override
	public Long zremrangeByLex(final String key, final String min, final String max) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.zremrangeByLex(key, min, max);
			}
		});
	}

	@Override
	public Long linsert(final String key, final LIST_POSITION where, final String pivot,
			final String value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.linsert(key, where, pivot, value);
			}
		});
	}

	@Override
	public Long lpushx(final String key, final String... string) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.lpushx(key, string);
			}
		});
	}

	@Override
	public Long rpushx(final String key, final String... string) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.rpushx(key, string);
			}
		});
	}

	@Override
	@Deprecated
	public List<String> blpop(final String arg) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.blpop(arg);
			}
		});
	}

	@Override
	public List<String> blpop(final int timeout, final String key) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.blpop(timeout, key);
			}
		});
	}

	@Override
	@Deprecated
	public List<String> brpop(final String arg) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.brpop(arg);
			}
		});
	}

	@Override
	public List<String> brpop(final int timeout, final String key) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.brpop(timeout, key);
			}
		});
	}

	@Override
	public Long del(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.del(key);
			}
		});
	}

	@Override
	public String echo(final String string) {
		return executeCache(new CallBack<String>() {
			@Override
			public String invoke(Jedis jedis) {
				return jedis.echo(string);
			}
		});
	}

	@Override
	public Long move(final String key, final int dbIndex) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.move(key, dbIndex);
			}
		});
	}

	@Override
	public Long bitcount(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.bitcount(key);
			}
		});
	}

	@Override
	public Long bitcount(final String key, final long start, final long end) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.bitcount(key, start, end);
			}
		});
	}

	@Override
	public Long bitpos(final String key, final boolean value) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.bitpos(key, value);
			}
		});
	}

	@Override
	public Long bitpos(final String key, final boolean value, final BitPosParams params) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.bitpos(key, value, params);
			}
		});
	}

	@Override
	@Deprecated
	public ScanResult<Entry<String, String>> hscan(final String key, final int cursor) {
		return executeCache(new CallBack<ScanResult<Entry<String, String>>>() {
			@Override
			public ScanResult<Entry<String, String>> invoke(Jedis jedis) {
				return jedis.hscan(key, cursor);
			}
		});
	}

	@Override
	@Deprecated
	public ScanResult<String> sscan(final String key, final int cursor) {
		return executeCache(new CallBack<ScanResult<String>>() {
			@Override
			public ScanResult<String> invoke(Jedis jedis) {
				return jedis.sscan(key, cursor);
			}
		});
	}

	@Override
	@Deprecated
	public ScanResult<Tuple> zscan(final String key, final int cursor) {
		return executeCache(new CallBack<ScanResult<Tuple>>() {
			@Override
			public ScanResult<Tuple> invoke(Jedis jedis) {
				return jedis.zscan(key, cursor);
			}
		});
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(final String key, final String cursor) {
		return executeCache(new CallBack<ScanResult<Entry<String, String>>>() {
			@Override
			public ScanResult<Entry<String, String>> invoke(Jedis jedis) {
				return jedis.hscan(key, cursor);
			}
		});
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(final String key, final String cursor,
			final ScanParams params) {
		return executeCache(new CallBack<ScanResult<Entry<String, String>>>() {
			@Override
			public ScanResult<Entry<String, String>> invoke(Jedis jedis) {
				return jedis.hscan(key, cursor, params);
			}
		});
	}

	@Override
	public ScanResult<String> sscan(final String key, final String cursor) {
		return executeCache(new CallBack<ScanResult<String>>() {
			@Override
			public ScanResult<String> invoke(Jedis jedis) {
				return jedis.sscan(key, cursor);
			}
		});
	}

	@Override
	public ScanResult<String> sscan(final String key, final String cursor, final ScanParams params) {
		return executeCache(new CallBack<ScanResult<String>>() {
			@Override
			public ScanResult<String> invoke(Jedis jedis) {
				return jedis.sscan(key, cursor, params);
			}
		});
	}

	@Override
	public ScanResult<Tuple> zscan(final String key, final String cursor) {
		return executeCache(new CallBack<ScanResult<Tuple>>() {
			@Override
			public ScanResult<Tuple> invoke(Jedis jedis) {
				return jedis.zscan(key, cursor);
			}
		});
	}

	@Override
	public ScanResult<Tuple> zscan(final String key, final String cursor, final ScanParams params) {
		return executeCache(new CallBack<ScanResult<Tuple>>() {
			@Override
			public ScanResult<Tuple> invoke(Jedis jedis) {
				return jedis.zscan(key, cursor, params);
			}
		});
	}

	@Override
	public Long pfadd(final String key, final String... elements) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.pfadd(key, elements);
			}
		});
	}

	@Override
	public long pfcount(final String key) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.pfcount(key);
			}
		});
	}

	@Override
	public Long geoadd(final String key, final double longitude, final double latitude,
			final String member) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.geoadd(key, longitude, latitude, member);
			}
		});
	}

	@Override
	public Long geoadd(final String key,
			final Map<String, GeoCoordinate> memberCoordinateMap) {
		return executeCache(new CallBack<Long>() {
			@Override
			public Long invoke(Jedis jedis) {
				return jedis.geoadd(key, memberCoordinateMap);
			}
		});
	}

	@Override
	public Double geodist(final String key, final String member1, final String member2) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.geodist(key, member1, member2);
			}
		});
	}

	@Override
	public Double geodist(final String key, final String member1, final String member2,
			final GeoUnit unit) {
		return executeCache(new CallBack<Double>() {
			@Override
			public Double invoke(Jedis jedis) {
				return jedis.geodist(key, member1, member2, unit);
			}
		});
	}

	@Override
	public List<String> geohash(final String key, final String... members) {
		return executeCache(new CallBack<List<String>>() {
			@Override
			public List<String> invoke(Jedis jedis) {
				return jedis.geohash(key, members);
			}
		});
	}

	@Override
	public List<GeoCoordinate> geopos(final String key, final String... members) {
		return executeCache(new CallBack<List<GeoCoordinate>>() {
			@Override
			public List<GeoCoordinate> invoke(Jedis jedis) {
				return jedis.geopos(key, members);
			}
		});
	}

	@Override
	public List<GeoRadiusResponse> georadius(final String key, final double longitude,
			final double latitude, final double radius, final GeoUnit unit) {
		return executeCache(new CallBack<List<GeoRadiusResponse>>() {
			@Override
			public List<GeoRadiusResponse> invoke(Jedis jedis) {
				return jedis.georadius(key, longitude, latitude, radius, unit);
			}
		});
	}

	@Override
	public List<GeoRadiusResponse> georadius(final String key, final double longitude,
			final double latitude, final double radius, final GeoUnit unit, final GeoRadiusParam param) {
		return executeCache(new CallBack<List<GeoRadiusResponse>>() {
			@Override
			public List<GeoRadiusResponse> invoke(Jedis jedis) {
				return jedis.georadius(key, longitude, latitude, radius, unit, param);
			}
		});
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(final String key, final String member,
			final double radius, final GeoUnit unit) {
		return executeCache(new CallBack<List<GeoRadiusResponse>>() {
			@Override
			public List<GeoRadiusResponse> invoke(Jedis jedis) {
				return jedis.georadiusByMember(key, member, radius, unit);
			}
		});
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(final String key, final String member,
			final double radius, final GeoUnit unit, final GeoRadiusParam param) {
		return executeCache(new CallBack<List<GeoRadiusResponse>>() {
			@Override
			public List<GeoRadiusResponse> invoke(Jedis jedis) {
				return jedis.georadiusByMember(key, member, radius, unit, param);
			}
		});
	}

}


