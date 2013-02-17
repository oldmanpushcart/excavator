package com.googlecode.excavator.consumer.message;

import java.net.InetSocketAddress;

import com.googlecode.excavator.consumer.ConsumerService;
import com.googlecode.excavator.message.Message;

/**
 * 链接变更消息
 * @author vlinux
 *
 */
public class ChannelChangedMessage extends Message<ConsumerService> {

	/**
	 * 消息类型
	 * @author vlinux
	 *
	 */
	public static enum Type {
		
		/**
		 * 创建channel
		 */
		CREATE,
		
		/**
		 * 删除channel
		 */
		REMOVE
		
	}
	
	private final Type type;					//消息类型
	private final InetSocketAddress address;	//地址
	private final String provider;				//服务提供方，应用名 
	
	public ChannelChangedMessage(ConsumerService t, String provider, InetSocketAddress address, Type type) {
		super(t);
		this.provider = provider;
		this.address = address;
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	public InetSocketAddress getAddress() {
		return address;
	}
	
	public String getProvider() {
		return provider;
	}

}
