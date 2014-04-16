package com.googlecode.excavator.provider;

import org.jboss.netty.channel.Channel;

import com.googlecode.excavator.protocol.Protocol;

/**
 * 业务工作者
 *
 * @author vlinux
 *
 */
public interface BusinessWorker {

    /**
     * 干活~
     *
     * @param proto
     * @param channel
     */
    void work(Protocol proto, Channel channel);

}
