package com.googlecode.excavator.message;

/**
 * 消息订阅者
 *
 * @author vlinux
 *
 */
public interface MessageSubscriber {

    /**
     * 接收消息
     *
     * @param msg
     * @throws Exception
     */
    void receive(Message<?> msg) throws Exception;

}
