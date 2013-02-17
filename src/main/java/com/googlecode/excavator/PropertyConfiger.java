package com.googlecode.excavator;

import static com.googlecode.excavator.constant.PropertyConfigerConstant.APPNAME;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.CONSUMER_CONNECT_TIMEOUT;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.MONITOR_ENABLE;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.PROFILER_ENABLE;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.PROFILER_LIMIT;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.PROVIDER_IP;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.PROVIDER_PORT;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.PROVIDER_WORKERS;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.SERIALIZER_NAME;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.TOKEN_ENABLE;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.ZK_CONNECT_TIMEOUT;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.ZK_SERVER_LIST;
import static com.googlecode.excavator.constant.PropertyConfigerConstant.ZK_SESSION_TIMEOUT;
import static com.googlecode.excavator.util.HostInfoUtil.getHostFirstIp;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isBlank;
import static com.googlecode.excavator.serializer.SerializerFactory.*;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.googlecode.excavator.constant.Log4jConstant;


/**
 * 从指定的property文件中获取配置信息
 * @author vlinux
 *
 */
public final class PropertyConfiger {

	private static final String PROPERTY_CLASSPATH = "/excavator.properties";
	private static final Logger logger = Logger.getLogger(Log4jConstant.CONFIG);
	
	private static final Properties properties = new Properties();
	
	// 是否开启监控
	private static boolean isEnableMonitor = false;
	
	// 应用名称
	private static String appname;
	
	// 本地提供服务的网络地址
	private static InetSocketAddress providerAddress;
	
	// 是否启用token
	private static boolean isEnableToken = false;
	
	// 是否启用性能监控
	private static boolean isEnableProfiler = false;
	
	// 性能告警阀值
	private static long profilerLimit = 100;
	
	// 序列化方式
	private static String serializerName = SERIALIZER_NAME_HESSIAN;
	
	
	
	
	/**
	 * 初始化
	 */
	static {
		
		InputStream is = null;
		try {
			is = PropertyConfiger.class.getResourceAsStream(PROPERTY_CLASSPATH);
			if( null == is ) {
				throw new IllegalStateException("excavator.properties can not found in the classpath.");
			}
			properties.load(is);
			
			// pre-load
			preloadMonitorEnable();
			preloadAppName();
			preloadTokenEnable();
			preloadProfilerLimit();
			preloadProfilerEnable();
			preloadSerializer();
			
		} catch(Throwable t) {
			logger.warn("load excavator's properties file failed.", t);
		} finally {
			if( null != is ) {
				try {is.close();}catch(Exception e) {}
			}//if
		}
		
	}
	
	
	/**
	 * 预加载序列化方式
	 */
	private static void preloadSerializer() {
		final String cfgSerName = properties.getProperty(SERIALIZER_NAME);
		if( !isBlank(cfgSerName) ) {
			serializerName = cfgSerName;
		}
		logger.info(format("%s=%s",SERIALIZER_NAME,serializerName));
	}
	
	/**
	 * 预加载性能开关
	 */
	private static void preloadProfilerEnable() {
		try {
			isEnableProfiler = Boolean.valueOf(properties.getProperty(PROFILER_ENABLE));
		}catch(Exception e) {
			//
		}
		logger.info(format("%s=%s",PROFILER_ENABLE,isEnableProfiler));
	}
	
	/**
	 * 预加载性能告警阀值
	 */
	private static void preloadProfilerLimit() {
		try {
			profilerLimit = Long.valueOf(properties.getProperty(PROFILER_LIMIT));
		}catch(Exception e) {
			//
		}
		logger.info(format("%s=%s",PROFILER_LIMIT,profilerLimit));
	}
	
	/**
	 * 预加载token追踪选项
	 */
	private static void preloadTokenEnable() {
		try {
			isEnableToken = Boolean.valueOf(properties.getProperty(TOKEN_ENABLE));
		}catch(Exception e) {
			//
		}
		logger.info(format("%s=%s",TOKEN_ENABLE,isEnableToken));
	}
	
	/**
	 * 判断字符串是否符合ip的格式
	 * @param ip
	 * @return
	 */
	private static boolean isIp(String ip) {
		return !isBlank(ip) 
				&& ip.matches("([0-9]{1,3}\\.{1}){3}[0-9]{1,3}");
	}
	
	/**
	 * 注入provider用于提供服务的网络信息<br/>
	 * 这个方法只会被provider所调用
	 */
	private static void preloadAddress() {
		
		final String ip;
		final String cfgIp = properties.getProperty(PROVIDER_IP);
		if( properties.containsKey(PROVIDER_IP) ) {
			// 如果你填写了ip地址，但是却不是一个有效的ip地址，则报错
			if( !isIp(cfgIp) ) {
				throw new IllegalArgumentException(
						format("%s=%s isn't an ip", PROVIDER_IP, properties.getProperty(PROVIDER_IP)));
			}
			ip = properties.getProperty(PROVIDER_IP);
		}
		// 如果没指定ip，则从网卡中找第一个
		else {
			ip = getHostFirstIp();
		}
		
		final int port;
		if( ! properties.containsKey(PROVIDER_PORT)
				|| isBlank(properties.getProperty(PROVIDER_PORT))) {
			throw new IllegalArgumentException(format("%s cant' be empty", PROVIDER_PORT));
		}
		
		try {
			port = Integer.valueOf(properties.getProperty(PROVIDER_PORT));
		}catch(Exception e) {
			throw new IllegalArgumentException(
					format("%s=%s illegal", PROVIDER_PORT, properties.getProperty(PROVIDER_PORT)), e);
		}
		
		providerAddress = new InetSocketAddress(ip,port);
		
		logger.info(format("address=%s",providerAddress));
	}
	
	/**
	 * 预加载monitor_enable选项<br/>
	 * 非必填，默认为false，即不打开监控
	 */
	private static void preloadMonitorEnable() {
		try {
			isEnableMonitor = Boolean.valueOf(properties.getProperty(MONITOR_ENABLE));
		}catch(Exception e) {
			//
		} finally {
			logger.info(format("%s=%s",MONITOR_ENABLE,isEnableMonitor));
		}
	}
	
	/**
	 * 注入本地应用名<br/>
	 * 必填，且仅限在字符和数字
	 */
	private static void preloadAppName() {
		if( ! properties.containsKey(APPNAME)
				|| isBlank(appname = properties.getProperty(APPNAME))) {
			throw new IllegalArgumentException(format("%s can't be empty", APPNAME));
		}
		if( !appname.matches("[\\w|-]+") ) {
			throw new IllegalArgumentException(format("%s must in [A-z0-9]", APPNAME));
		}
		logger.info(format("%s=%s",APPNAME,appname));
	}
	
	/**
	 * 懒加载获取服务端提供服务的address
	 * @return
	 */
	public final static InetSocketAddress getProviderAddress() {
		// 这里没太多并发，大家可以华丽丽的忽略dcl
		if( null == providerAddress ) {
			synchronized (PropertyConfiger.class) {
				if( null == providerAddress ) {
					preloadAddress();
				}
			}
		}
		return providerAddress;
	}
	
	/**
	 * 获取服务端工作线程数
	 * @return
	 */
	public static int getProviderWorkers() {
		return Integer.valueOf(properties.getProperty(PROVIDER_WORKERS));
	}
	
	/**
	 * 获取应用名
	 * @return
	 */
	public static String getAppName() {
		return appname;
	}
	
	/**
	 * 获取zk连接超时时间
	 * @return
	 */
	public static int getZkConnectTimeout() {
		return Integer.valueOf(properties.getProperty(ZK_CONNECT_TIMEOUT));
	}
	
	/**
	 * 获取zk会话超时时间
	 * @return
	 */
	public static int getZkSessionTimeout() {
		return Integer.valueOf(properties.getProperty(ZK_SESSION_TIMEOUT));
	}
	
	/**
	 * 获取zk的服务器列表
	 * @return
	 */
	public static String getZkServerList() {
		return properties.getProperty(ZK_SERVER_LIST);
	}
	
	/**
	 * 客户端获取配置：连接超时时间
	 * @return
	 */
	public static int getConsumerConnectTimeout() {
		return Integer.valueOf(properties.getProperty(CONSUMER_CONNECT_TIMEOUT));
	}
	
	/**
	 * 是否开启监控
	 * @return
	 */
	public static boolean isEnableMonitor() {
		return isEnableMonitor;
	}
	
	/**
	 * 是否开启token跟踪<br/>
	 * RmiTrace的token生成策略<br/>
	 * false表明不内部不自行生成token(UUID) true表明如果对方不传token，RmiTrace将会自动帮忙创建一个新的token
	 * 
	 * @return
	 */
	public static boolean isEnableToken() {
		return isEnableToken;
	}

	/**
	 * 获取性能告警阀值<br/>
	 * 当timeout-cost小于等于limit时打印出告警信息
	 * @return
	 */
	public static long getProfilerLimit() {
		return profilerLimit;
	}

	/**
	 * 获取性能开关
	 * @return
	 */
	public static boolean isEnableProfiler() {
		return isEnableProfiler;
	}
	
	/**
	 * 取得序列化方式名称
	 * @return
	 */
	public static String getSerializerName() {
		return serializerName;
	}
	
}
