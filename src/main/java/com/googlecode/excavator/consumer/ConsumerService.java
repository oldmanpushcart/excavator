package com.googlecode.excavator.consumer;

/**
 * consumer的service
 * @author vlinux
 *
 */
public class ConsumerService {

	private final String key;			//group+version+sign
	private final String group;			//服务分组
	private final String version;		//服务版本
	private final String sign;			//服务签名
	private final long timeout;			//超时时间
	

	public ConsumerService(String group,String version, String sign, long timeout) {
		this.group = group;
		this.version = version;
		this.sign = sign;
		this.timeout = timeout;
		this.key = group+version+sign;
	}

	public String getSign() {
		return sign;
	}

	public String getGroup() {
		return group;
	}

	public String getVersion() {
		return version;
	}

	public long getTimeout() {
		return timeout;
	}

	public String getKey() {
		return key;
	}
	
	public String toString() {
		return String.format("sign=%s;group=%s;version=%s;timeout=%s", 
				sign,group,version,timeout);
	}

}
