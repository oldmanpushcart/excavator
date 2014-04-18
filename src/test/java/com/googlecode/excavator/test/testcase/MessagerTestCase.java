package com.googlecode.excavator.test.testcase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.googlecode.excavator.message.MemeryMessager;
import com.googlecode.excavator.message.Message;
import com.googlecode.excavator.message.MessageSubscriber;
import com.googlecode.excavator.message.Messager;

public class MessagerTestCase {

    @Test
    public void test_for_messager_post_normal() throws Exception {
        
        final int total = 100;
        final String words = "fuckyou";
        final CountDownLatch countDown = new CountDownLatch(total);
        final Messager messager = new MemeryMessager();
        final AtomicInteger counter = new AtomicInteger();
        messager.register(new MessageSubscriber() {
            
            @Override
            public void receive(Message<?> msg) throws Exception {
                try {
                    if( null != msg
                            && msg.getReTry() == 1
                            && null != msg.getContent()
                            && msg.getContent() instanceof String
                            && StringUtils.equals(msg.getContent().toString(), words)) {
                        counter.incrementAndGet();
                    }
                } finally {
                    countDown.countDown();
                }
                
            }
        }, Message.class);
        
        for( int i=0;i<total;i++ ) {
            messager.post(new Message<String>(words));
        }
        
        Assert.assertTrue(countDown.await(10, TimeUnit.SECONDS));
        Assert.assertTrue(total == counter.get());
        
        
    }
    
    @Test
    public void test_for_messager_post_repost() throws Exception {
        
        final int total = 100;
        final String words = "fuckyou";
        final CountDownLatch countDown = new CountDownLatch(total);
        final Messager messager = new MemeryMessager();
        final AtomicInteger counter = new AtomicInteger();
        messager.register(new MessageSubscriber() {
            
            @Override
            public void receive(Message<?> msg) throws Exception {
                if( null != msg
                        && msg.getReTry() <=2 ) {
                    throw new Exception("retry...");
                }
                try {
                    
                    if( null != msg
                            && msg.getReTry() == 3
                            && null != msg.getContent()
                            && msg.getContent() instanceof String
                            && StringUtils.equals(msg.getContent().toString(), words)) {
                        counter.incrementAndGet();
                    }
                } finally {
                    countDown.countDown();
                }
                
            }
        }, Message.class);
        
        for( int i=0;i<total;i++ ) {
            messager.post(new Message<String>(words));
        }
        
        Assert.assertTrue(countDown.await(10, TimeUnit.SECONDS));
        Assert.assertTrue(total == counter.get());
        
    }
    
}
