package com.googlecode.excavator.spring;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.googlecode.excavator.consumer.ConsumerProxyFactory;

/**
 * ÍÚ¾ò»úrmiÏû·Ñ¶ËSpringÅäÖÃbean
 *
 * @author vlinux
 *
 */
public class ExcavatorConsumerSpringFactoryBean implements InitializingBean, FactoryBean {

    private Class<?> targetInterface;
    private String group;
    private String version;
    private long defaultTimeout;
    private Map<String, Long> methodTimeoutMap;

    private ConsumerProxyFactory factory;

    @Override
    public Object getObject() throws Exception {
        return factory.proxy(targetInterface, group, version, defaultTimeout, methodTimeoutMap);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        factory = ConsumerProxyFactory.singleton();
    }

    @Override
    public Class<?> getObjectType() {
        return targetInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setTargetInterface(Class<?> targetInterface) {
        this.targetInterface = targetInterface;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public void setMethodTimeoutMap(Map<String, Long> methodTimeoutMap) {
        this.methodTimeoutMap = methodTimeoutMap;
    }

    public void setConsumerProxyFactory(ConsumerProxyFactory factory) {
        this.factory = factory;
    }

}
