package com.googlecode.excavator.monitor;

/**
 * 日志格式
 * 
 * @author vlinux
 * 
 */
public final class Monitor {

	/**
	 * 日志类型
	 * 
	 * @author vlinux
	 * 
	 */
	public static enum Type {
		/**
		 * 服务端
		 */
		PROVIDER,
		/**
		 * 消费端
		 */
		CONSUMER
	}

	private final Type type; 		// 类型
	private final String group; 	// 调用分组
	private final String version; 	// 调用版本
	private final String sign; 		// 调用签名
	private final String from; 		// 来源
	private final String to; 		// 目的
	private long times; 			// 周期内调用次数
	private long cost; 				// 周期内消费时间ms

	/**
	 * 构造函数，用于第一次创建monitor信息
	 * @param type
	 * @param group
	 * @param version
	 * @param sign
	 * @param from
	 * @param to
	 */
	public Monitor(Type type, String group, String version, String sign,
			String from, String to) {
		this.type = type;
		this.group = group;
		this.version = version;
		this.sign = sign;
		this.from = from;
		this.to = to;
	}

	/**
	 * 构造函数，用于每次monitor信息的累加
	 * @param m
	 */
	public Monitor(Monitor m) {
		this.type = m.type;
		this.group = m.group;
		this.version = m.version;
		this.sign = m.sign;
		this.from = m.from;
		this.to = m.to;
	}

	public Type getType() {
		return type;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

	public long getCost() {
		return cost;
	}

	public void setCost(long cost) {
		this.cost = cost;
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
	
	@Override
	protected Monitor clone() {
		final Monitor m = new Monitor(this);
		m.cost = cost;
		m.times = times;
		return m;
	}
	
}
