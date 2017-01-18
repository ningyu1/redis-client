/*
 * Copyright (c) 2017, Tsoft and/or its affiliates. All rights reserved.
 * FileName: CacheUtils.java
 * Author:   ningyu
 * Date:     2017年1月18日
 * Description: //模块目的、功能描述      
 * History: //修改记录
 * <author>      <time>      <version>    <desc>
 * 修改人姓名             修改时间            版本号                  描述
 */
package cn.tsoft.framework.redis.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <功能描述>
 * @author ningyu
 * @date 2017年1月18日 上午9:52:34
 */
public class CacheUtils {
	
	/**
     * log
     */
    private static Logger logger = LoggerFactory.getLogger(CacheUtils.class);
    
    public static String getObjectType(Object object) {
		String res= "";
		if (object instanceof String) {
			res = "String";
		} else if (object instanceof byte[]) {
			res = "byte[]";
		} else if (object instanceof List) {
			res = "List";
		} else if (object instanceof Map) {
			res = "Map";
		} else if (object instanceof Set) {
			res = "Set";
		} else if (object instanceof Integer) {
			res = "Integer";
		} else if (object instanceof Double) {
			res = "Double";
		} else if (object instanceof Float) {
			res = "Float";
		} else if (object instanceof Long) {
			res = "Long";
		} else if (object instanceof Boolean) {
			res = "Boolean";
		} else {
			res = "other";
		}
		return res;
	}

	public static String getObjectSize(Object object) {
		Integer res= 0;
		if (object instanceof String) {
			String s = (String) object;
			res = s.length();
		} else if (object instanceof byte[]) {
			byte[] b = (byte[]) object;
			res = b.length;
		} else if (object instanceof List) {
			List<?> l = (List<?>) object;
			res = l.size();
		} else if (object instanceof Map) {
			Map m = (Map) object;
			res = m.size();
		} else if (object instanceof Set) {
			Set m = (Set) object;
			res = m.size();
		} else if (object instanceof Integer) {
			res = Integer.SIZE;
		} else if (object instanceof Double) {
			res = Double.SIZE;
		} else if (object instanceof Float) {
			res = Float.SIZE;
		} else if (object instanceof Long) {
			res = Long.SIZE;
		} else if (object instanceof Boolean) {
			//真的是1...
			res = 1;
		} else {
			//res = JSON.toJSONString(object).length();
			//性能开销太大
			return "unknown";
		}
		return res+"";
	}

}


