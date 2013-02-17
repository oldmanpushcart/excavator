package com.googlecode.excavator.spring;

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.googlecode.excavator.provider.ProviderProxyFactory;

/**
 * ÍÚ¾ò»úrmi·þÎñ¶ËSpringÅäÖÃbean
 * @author vlinux
 *
 */
public class ExcavatorProviderSpringFactoryBean implements InitializingBean, FactoryBean {

	private Class<?> targetInterface;
	private String group;
	private String version;
	private long defaultTimeout;
	private Map<String, Long> methodTimeoutMap;
	
	private Object targetObject;
	private ProviderProxyFactory factory;

	@Override
	public void afterPropertiesSet() throws Exception {
		factory = ProviderProxyFactory.singleton();
	}
	
	@Override
	public Object getObject() throws Exception {
		return factory.proxy(targetInterface, targetObject, group, version, defaultTimeout, methodTimeoutMap);
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

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
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

}
