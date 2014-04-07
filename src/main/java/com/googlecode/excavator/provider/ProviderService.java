package com.googlecode.excavator.provider;

import java.lang.reflect.Method;

/**
 * 服务提供方的service数据结构
 *
 * @author vlinux
 *
 */
public final class ProviderService {

    private final String key;			//group+version+sign
    private final String group;			//服务分组
    private final String version;		//服务版本
    private final String sign;			//服务签名
    private final Class<?> serviceItf;	//服务接口
    private final Object serviceObject;	//服务对象
    private final Method serviceMethod;	//服务方法
    private final long timeout;			//超时时间

    /**
     * 构造服务端对service的数据
     *
     * @param group
     * @param version
     * @param sign
     * @param serviceObject
     * @param serviceMethod
     * @param timeout
     */
    public ProviderService(
            String group, String version, String sign,
            Class<?> serviceItf, Object serviceObject, Method serviceMethod, long timeout) {
        this.group = group;
        this.version = version;
        this.sign = sign;
        this.serviceItf = serviceItf;
        this.serviceObject = serviceObject;
        this.serviceMethod = serviceMethod;
        this.timeout = timeout;
        this.key = group + version + sign;
    }

    public String getKey() {
        return key;
    }

    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public String getSign() {
        return sign;
    }

    public Object getServiceObject() {
        return serviceObject;
    }

    public Method getServiceMethod() {
        return serviceMethod;
    }

    public long getTimeout() {
        return timeout;
    }

    public Class<?> getServiceItf() {
        return serviceItf;
    }

    public String toString() {
        return String.format("group=%s;version=%s;sign=%s;timeout=%s",
                group, version, sign, timeout);
    }

}
