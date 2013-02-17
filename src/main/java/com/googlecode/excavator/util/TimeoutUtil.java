package com.googlecode.excavator.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import com.google.common.collect.Maps;

/**
 * 用于计算超时的工具类
 * @author vlinux
 *
 */
public final class TimeoutUtil {

	// getFixTimeout方法的缓存
	private static final Map<Method, Long> methodTimeoutCache = Maps.newConcurrentMap();
	
	/**
	 * 返回修正过后的超时时间
	 * @param method
	 * @return
	 */
	public static long getFixTimeout(Method method, long defaultTimeout, Map<String, Long> methodTimeoutMap) {
		
		if( methodTimeoutCache.containsKey(method) ) {
			return methodTimeoutCache.get(method);
		}
		
		if( MapUtils.isNotEmpty(methodTimeoutMap)
				&& methodTimeoutMap.containsKey(method.getName())) {
			Long timeout = methodTimeoutMap.get(method.getName());
			if( null != timeout
					&& timeout > 0) {
				return timeout;
			}//if
		}//if
		
		final long timeout = defaultTimeout > 0 ? defaultTimeout : 500 ;
		methodTimeoutCache.put(method, timeout);
		return timeout;
		
	}
	
}
