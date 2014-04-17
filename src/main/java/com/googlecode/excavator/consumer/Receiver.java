package com.googlecode.excavator.consumer;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.netty.channel.Channel;

import com.googlecode.excavator.protocol.Protocol;

/**
 * 接收者
 *
 * @author vlinux
 *
 */
public interface Receiver {

    /**
     * 包装类，在此包装中封装了rmi请求和应答
     *
     * @author vlinux
     *
     */
    public final static class Wrapper {

//        private final RmiRequest request;
        private final Protocol request;
        private Protocol response;
        private final ReentrantLock lock;
        private final Condition waitResp;
        private Channel channel;

        public Wrapper(Protocol request) {
            this.request = request;
            this.lock = new ReentrantLock(false);
            this.waitResp = lock.newCondition();
        }

        /**
         * 唤醒响应等待
         */
        public void signalWaitResp() {
            lock.lock();
            try {
                waitResp.signal();
            } finally {
                lock.unlock();
            }
        }

        public Protocol getRequest() {
            return request;
        }

        public Protocol getResponse() {
            return response;
        }

        public void setResponse(Protocol response) {
            this.response = response;
        }

        public ReentrantLock getLock() {
            return lock;
        }

        public Condition getWaitResp() {
            return waitResp;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

    }

    /**
     * 挂号一个请求
     *
     * @param req
     * @return
     */
    Receiver.Wrapper register(Protocol req);

    /**
     * 删除之前挂号的请求
     *
     * @param id
     * @return
     */
    Receiver.Wrapper unRegister(long id);

    /**
     * 删除channel上所有的挂号
     *
     * @param channel
     * @return
     */
    List<Receiver.Wrapper> unRegister(Channel channel);

    /**
     * 接收返回的讯息<br/>
     * 讯息一旦接收，将会从接收池中去掉
     *
     * @param id
     * @return 返回对应的包装类 如果reqEvtId不存在则返回null<br/>
     */
    Receiver.Wrapper receive(long id);

}
