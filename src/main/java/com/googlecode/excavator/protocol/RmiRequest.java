package com.googlecode.excavator.protocol;

import java.io.Serializable;

/**
 * rmi调用协议:请求
 * @author vlinux
 *
 */
public final class RmiRequest extends RmiTracer implements Serializable {

	private static final long serialVersionUID = 823825583753849024L;
	
	private final String key;			//group+version+sign
	private final String group;			//服务分组
	private final String version;		//服务版本
	private final String sign;			//服务签名
	private final Serializable[] args;	//传递参数
	private final String appName;		//消费端应用名
	private final long timeout;			//请求超时时长
	
	/**
	 * 构造rmi请求
	 * @param group
	 * @param version
	 * @param sign
	 * @param args
	 * @param appName
	 * @param timeout
	 */
	public RmiRequest(
			String group, String version, String sign, 
			Serializable[] args, String appName, long timeout) {
		this.group = group;
		this.version = version;
		this.sign = sign;
		this.args = args;
		this.appName = appName;
		this.timeout = timeout;
		this.key = group+version+sign;
	}
	
	/**
	 * rmi请求:转发用<br/>
	 * 通过此构造函数出来的请求对象将会携有指定的token
	 * @param token
	 * @param group
	 * @param version
	 * @param sign
	 * @param args
	 * @param appName
	 * @param timeout
	 */
	public RmiRequest(String token, String group, String version, String sign, 
			Serializable[] args, String appName, long timeout) {
		super(token);
		this.group = group;
		this.version = version;
		this.sign = sign;
		this.args = args;
		this.appName = appName;
		this.timeout = timeout;
		this.key = group+version+sign;
	}
	
	public String getKey() {
		return key;
	}

	public String getAppName() {
		return appName;
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

	public Serializable[] getArgs() {
		return args;
	}

	public long getTimeout() {
		return timeout;
	}

	@Override
	public String toString() {
		return String.format("REQ[id=%s;token=%s;group=%s;version=%s;sign=%s;consumer=%s;timeout=%s;]",
				getId(), getToken(), group, version, sign, appName, timeout );
	}
	
}
