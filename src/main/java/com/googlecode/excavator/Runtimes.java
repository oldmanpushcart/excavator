package com.googlecode.excavator;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import com.googlecode.excavator.protocol.RmiRequest;

/**
 * 挖掘机运行时runtime
 *
 * @author vlinux
 *
 */
public final class Runtimes {

    /**
     * 运行时状态
     */
    private static ThreadLocal<Runtime> runtimes = new ThreadLocal<Runtime>();

    /**
     * 获取当前运行时状态
     *
     * @return
     */
    public static Runtime getRuntime() {
        return runtimes.get();
    }

    /**
     * 注入runtime
     *
     * @param runtime
     * @return
     */
    public static Runtime inject(Runtime runtime) {
        runtimes.set(runtime);
        return runtime;
    }

    /**
     * 移出当前runtime
     *
     * @return
     */
    public static Runtime remove() {
        Runtime runtime = runtimes.get();
        if (null != runtime) {
            runtimes.remove();
        }
        return runtime;
    }

    /**
     * runtime信息
     *
     * @author vlinux
     *
     */
    public static final class Runtime {

        private final RmiRequest req;				//消费方请求
        private final String consumer;				//请求方应用名
        private final String provider;				//服务方应用名
        private final Class<?> serviceInterface;	//服务接口
        private final Method serviceMtd;			//服务方法
        private final InetSocketAddress consumerAddress;	//请求方网络地址(ip:port)
        private final InetSocketAddress providerAddress;	//服务方网络地址(ip:port)

        public Runtime(RmiRequest req,
                String provider,
                Class<?> serviceInterface, Method serviceMtd,
                InetSocketAddress consumerAddress,
                InetSocketAddress providerAddress) {
            this.consumer = req.getAppName();
            this.provider = provider;
            this.serviceInterface = serviceInterface;
            this.serviceMtd = serviceMtd;
            this.consumerAddress = consumerAddress;
            this.providerAddress = providerAddress;
            this.req = req;
        }

        public Runtime(RmiRequest req, Class<?> serviceItf, Method serviceMtd) {
            this(req, null, serviceItf, serviceMtd, null, null);
        }

        public String getConsumer() {
            return consumer;
        }

        public String getProvider() {
            return provider;
        }

        public InetSocketAddress getConsumerAddress() {
            return consumerAddress;
        }

        public InetSocketAddress getProviderAddress() {
            return providerAddress;
        }

        public RmiRequest getReq() {
            return req;
        }

        public Class<?> getServiceInterface() {
            return serviceInterface;
        }

        public Method getServiceMtd() {
            return serviceMtd;
        }

    }

}
