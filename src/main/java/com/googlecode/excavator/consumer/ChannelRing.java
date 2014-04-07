package com.googlecode.excavator.consumer;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.channel.Channel;

import com.googlecode.excavator.protocol.RmiRequest;

/**
 * 链接环
 *
 * @author vlinux
 *
 */
public interface ChannelRing {

    /**
     * 链接包装类
     *
     * @author vlinux
     *
     */
    public static final class Wrapper {

        private final Channel channel;			//链接
        private final String provider;			//服务提供方应用名称
        private boolean maybeDown;				//可能损坏
        private final AtomicInteger counter;	//引用计数

        public Wrapper(Channel channel, String provider) {
            this.channel = channel;
            this.provider = provider;
            this.counter = new AtomicInteger();
        }

        public boolean isMaybeDown() {
            return maybeDown;
        }

        public void setMaybeDown(boolean maybeDown) {
            this.maybeDown = maybeDown;
        }

        public Channel getChannel() {
            return channel;
        }

        public String getProvider() {
            return provider;
        }

        /**
         * 引用++
         */
        public void inc() {
            counter.incrementAndGet();
        }

        /**
         * 引用--
         */
        public void dec() {
            if (0 >= counter.decrementAndGet()
                    && null != channel) {
                channel.disconnect();
                channel.close();
            }

        }

    }

    /**
     * 根据请求获取所需的链接
     *
     * @param req
     * @return
     */
    ChannelRing.Wrapper ring(RmiRequest req);

}
