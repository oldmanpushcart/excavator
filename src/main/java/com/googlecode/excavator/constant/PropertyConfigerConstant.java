package com.googlecode.excavator.constant;

/**
 * 配置key的一些常量
 * @author vlinux
 *
 */
public class PropertyConfigerConstant {

	/**
	 * 应用名称
	 */
	public static final String APPNAME = "excavator.appname";

	/**
	 * 连接zk的超时时间
	 */
	public static final String ZK_CONNECT_TIMEOUT = "excavator.zookeeper.connectTimeout";

	/**
	 * zk的会话超时时间
	 */
	public static final String ZK_SESSION_TIMEOUT = "excavator.zookeeper.sessionTimeout";

	/**
	 * zk的服务器列表
	 */
	public static final String ZK_SERVER_LIST = "excavator.zookeeper.servers";

	/**
	 * 客户端：访问服务器的超时时间
	 */
	public static final String CONSUMER_CONNECT_TIMEOUT = "excavator.consumer.connectTimeout";

	/**
	 * 服务端：工作线程数
	 */
	public static final String PROVIDER_WORKERS = "excavator.provider.workers";

	/**
	 * 服务端：服务提供地址
	 */
	public static final String PROVIDER_IP = "excavator.provider.ip";

	/**
	 * 服务端：服务提供端口
	 */
	public static final String PROVIDER_PORT = "excavator.provider.port";

	/**
	 * 是否打开监控
	 */
	public static final String MONITOR_ENABLE = "excavator.monitor.enable";

	/**
	 * 是否启用：token
	 */
	public static final String TOKEN_ENABLE = "excavator.token";

	/**
	 * 是否启用：profiler
	 */
	public static final String PROFILER_ENABLE = "excavator.profiler.enable";

	/**
	 * 性能比率
	 */
	public static final String PROFILER_LIMIT = "excavator.profiler.limit";

	/**
	 * 序列化方式名称
	 */
	public static final String SERIALIZER_NAME = "excavator.serializer.name";

}
