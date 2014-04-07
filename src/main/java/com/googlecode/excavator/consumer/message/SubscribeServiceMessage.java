package com.googlecode.excavator.consumer.message;

import com.googlecode.excavator.consumer.ConsumerService;
import com.googlecode.excavator.message.Message;

/**
 * 订阅服务消息
 *
 * @author vlinux
 *
 */
public class SubscribeServiceMessage extends Message<ConsumerService> {

    public SubscribeServiceMessage(ConsumerService t) {
        super(t);
    }

}
