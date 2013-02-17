package com.googlecode.excavator;

import java.util.Map;

/**
 * 代理工厂
 * @author vlinux
 *
 */
public interface ProxyFactory {

	
	<T> T proxy(Class<T> targetInterface, String group, String version, long defaultTimeout, Map<String, Long> methodTimeoutMap) throws Exception;
	
}
