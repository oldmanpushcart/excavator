package com.googlecode.excavator;

import java.util.Map;

/**
 * 代理工厂
 *
 * @author vlinux
 *
 */
public interface ProxyFactory {

    /**
     * 代理具体的类
     * @param targetInterface
     * @param group
     * @param version
     * @param defaultTimeout
     * @param methodTimeoutMap
     * @return
     * @throws Exception
     */
    <T> T proxy(Class<T> targetInterface, String group, String version, long defaultTimeout, Map<String, Long> methodTimeoutMap) throws Exception;

}
