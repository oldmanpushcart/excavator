package com.googlecode.excavator.message;


/**
 * 消息投递员<br/>
 * 用于程序内部多个support之间的相互通讯
 *
 * @author vlinux
 *
 */
public interface Messager {

    /**
     * 注册消息订阅者
     * @param subscriber
     * @param msgTypes
     */
    void register(MessageSubscriber subscriber, Class<?>... msgTypes);
    
    /**
     * 投递消息
     * @param msg
     */
    void post(Message<?> msg);
    
}
