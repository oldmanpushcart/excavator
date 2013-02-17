package com.googlecode.excavator.provider;

import org.jboss.netty.channel.Channel;

import com.googlecode.excavator.protocol.RmiRequest;

/**
 * 业务工作者
 * @author vlinux
 *
 */
public interface BusinessWorker {

	/**
	 * 干活~
	 * @param req
	 * @param channel
	 */
	void work(RmiRequest req, Channel channel);
	
}
