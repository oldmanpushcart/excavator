package com.googlecode.excavator.consumer;

import static com.googlecode.excavator.PropertyConfiger.getAppName;
import static com.googlecode.excavator.advice.Advices.doAfter;
import static com.googlecode.excavator.advice.Advices.doBefores;
import static com.googlecode.excavator.advice.Advices.doFinally;
import static com.googlecode.excavator.advice.Advices.doThrow;
import static com.googlecode.excavator.advice.Direction.Type.CONSUMER;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_SERVICE_NOT_FOUND;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_TIMEOUT;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_SUCCESSED_RETURN;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_SUCCESSED_THROWABLE;
import static com.googlecode.excavator.util.ExceptionUtil.hasNetworkException;
import static com.googlecode.excavator.util.SerializerUtil.changeToSerializable;
import static com.googlecode.excavator.util.SerializerUtil.isSerializableType;
import static com.googlecode.excavator.util.SignatureUtil.signature;
import static com.googlecode.excavator.util.TimeoutUtil.getFixTimeout;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang.StringUtils.isBlank;
import static java.lang.String.format;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.excavator.Runtimes;
import com.googlecode.excavator.constant.LogConstant;
import com.googlecode.excavator.consumer.message.SubscribeServiceMessage;
import com.googlecode.excavator.consumer.support.ConsumerSupport;
import com.googlecode.excavator.exception.ProviderNotFoundException;
import com.googlecode.excavator.exception.ServiceNotFoundException;
import com.googlecode.excavator.exception.ThreadPoolOverflowException;
import com.googlecode.excavator.exception.UnknowCodeException;
import com.googlecode.excavator.message.MemeryMessager;
import com.googlecode.excavator.message.Messager;
import com.googlecode.excavator.protocol.RmiRequest;
import com.googlecode.excavator.protocol.RmiResponse;

/**
 * 消费者代理工厂
 *
 * @author vlinux
 *
 */
public class ConsumerProxyFactory {

    private final Logger networkLog = LoggerFactory.getLogger(LogConstant.NETWORK);
    private final Logger agentLog = LoggerFactory.getLogger(LogConstant.AGENT);

    private final ConsumerSupport support;
    private final Messager messager;

    private ConsumerProxyFactory() throws Exception {
        messager = new MemeryMessager();
        support = new ConsumerSupport(messager);
        support.init();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    support.destroy();
                } catch (Exception e) {
                    agentLog.warn("destory consumer support failed.", e);
                }
            }
        });
    }

    /**
     * 生成客户端代理处理
     *
     * @param targetInterface
     * @param group
     * @param version
     * @param defaultTimeout
     * @param methodTimeoutMap
     * @return
     */
    private InvocationHandler createConsumerProxyHandler(
            final Class<?> targetInterface, 
            final String group, 
            final String version, 
            final long defaultTimeout, 
            final Map<String, Long> methodTimeoutMap) {
        return new InvocationHandler() {

            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {

                final String sign = signature(method);
                final long timeout = getFixTimeout(method, defaultTimeout, methodTimeoutMap);
                final RmiRequest req = generateRmiRequest(group, version, args, sign, timeout);
                final Receiver.Wrapper receiverWrapper = registerRecevier(req);

                final ReentrantLock lock = receiverWrapper.getLock();
                final long start = currentTimeMillis();
                final ChannelRing.Wrapper channelWrapper;
                Runtimes.Runtime runtime = null;
                lock.lock();
                try {
                    try {
                        channelWrapper = takeChannelRingWrapper(req);

                        // 将channel注入到请求channel中
                        receiverWrapper.setChannel(channelWrapper.getChannel());

                        // 生成运行时环境
                        runtime = new Runtimes.Runtime(req, channelWrapper.getProvider(),
                                targetInterface, method,
                                (InetSocketAddress) channelWrapper.getChannel().getLocalAddress(),
                                (InetSocketAddress) channelWrapper.getChannel().getRemoteAddress());
                        doBefores(CONSUMER, runtime);
                        waitForWrite(req, channelWrapper);
                    } catch (Throwable t) {
                        support.unRegister(req.getId());
                        throw t;
                    }

                    final long leftTimeout = timeout - (currentTimeMillis() - start);
                    if (leftTimeout <= 0) {
                        throw new TimeoutException(format("request:%s is waiting for write ready, but timeout:%dms", req, timeout));
                    }
                    waitForReceive(receiverWrapper, req, leftTimeout);

                    Object returnObj = getReturnObject(receiverWrapper, channelWrapper);
                    doAfter(CONSUMER, runtime, returnObj, currentTimeMillis() - start/*cost*/);
                    return returnObj;
                } catch (Throwable t) {
                    if (null == runtime) {
                        runtime = new Runtimes.Runtime(req, targetInterface, method);
                    }
                    doThrow(CONSUMER, runtime, t);
                    throw t;
                } finally {
                    lock.unlock();
                    doFinally(CONSUMER, runtime);
                }

            }

            /**
             * 注册接收者
             *
             * @param req
             * @return
             * @throws ProviderNotFoundException
             */
            private Receiver.Wrapper registerRecevier(final RmiRequest req)
                    throws ProviderNotFoundException {
                final Receiver.Wrapper receiverWrapper = support.register(req);
                // 没收到receiverWrapper说明服务不存在
                if (null == receiverWrapper) {
                    throw new ProviderNotFoundException(format("provider not found. req:%s", req));
                }
                return receiverWrapper;
            }

            /**
             * 生成req请求
             *
             * @param group
             * @param version
             * @param args
             * @param sign
             * @param timeout
             * @return
             */
            private RmiRequest generateRmiRequest(final String group,
                    final String version, Object[] args, final String sign,
                    final long timeout) {
                final RmiRequest req;

                final Runtimes.Runtime runtime = Runtimes.getRuntime();

                // 如果是第一次请求，需要主动生成token
                if (null == runtime) {
                    req = new RmiRequest(
                            group, version, sign,
                            changeToSerializable(args), getAppName(), timeout);
                } // 如果不是第一次请求，则需要将原来的token带上
                else {
                    req = new RmiRequest(
                            runtime.getReq().getToken(),
                            group, version, sign,
                            changeToSerializable(args), getAppName(), timeout);
                }
                return req;
            }

        };
    }

    /**
     * 代理对象
     *
     * @param targetInterface
     * @param group
     * @param version
     * @param defaultTimeout
     * @param methodTimeoutMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T proxy(Class<T> targetInterface, String group, String version, long defaultTimeout, Map<String, Long> methodTimeoutMap) throws Exception {

        // 检查参数
        check(targetInterface, group, version);

        // 消费端代理处理
        final InvocationHandler consumerProxyHandler = createConsumerProxyHandler(targetInterface, group, version, defaultTimeout, methodTimeoutMap);

        // 构造目标代理对象
        Object proxyObject = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{targetInterface}, consumerProxyHandler);

        // 注册服务
        registerService(targetInterface, group, version, defaultTimeout, methodTimeoutMap);

        return (T) proxyObject;

    }

    /**
     * 获取channelRing的包装<br/>
     * 本质上就是获取一个可用的channel了
     *
     * @param req
     * @return
     * @throws Throwable
     */
    private ChannelRing.Wrapper takeChannelRingWrapper(RmiRequest req) throws Throwable {
        ChannelRing.Wrapper channelWrapper = support.ring(req);
        if (null == channelWrapper) {
            throw new ProviderNotFoundException(format("provider not found. req:%s", req));
        }
        return channelWrapper;
    }

    /**
     * 写请求
     *
     * @param reqEvt
     * @param channelWrapper
     * @throws Throwable
     */
    private void waitForWrite(RmiRequest req, ChannelRing.Wrapper channelWrapper) throws Throwable {
        ChannelFuture future = channelWrapper.getChannel().write(req);
        future.awaitUninterruptibly();
        if (!future.isSuccess()) {
            Throwable cause = future.getCause();
            if (hasNetworkException(cause)) {
                channelWrapper.setMaybeDown(true);
            }
            networkLog.warn("write req:{} failed. maybeDown:{}", new Object[]{req, channelWrapper.isMaybeDown(), future.getCause()});
            throw future.getCause();
        }
    }

    /**
     * 等候回复通知
     *
     * @oaram wrapper
     * @param req
     * @param leftTimeout
     * @return
     * @throws TimeoutException
     * @throws ProviderNotFoundException
     * @throws InterruptedException
     */
    private Receiver.Wrapper waitForReceive(Receiver.Wrapper wrapper, RmiRequest req, long leftTimeout)
            throws TimeoutException, ProviderNotFoundException, InterruptedException {

        // 等待消息
        wrapper.getWaitResp().await(leftTimeout, TimeUnit.MILLISECONDS);

        // 没收到response事件说明是超时
        if (null == wrapper.getResponse()) {
            throw new TimeoutException(format("req:%s timeout:%dms", req, req.getTimeout()));
        }

        return wrapper;

    }

    /**
     * 获取return的对象
     *
     * @param receiverWrapper
     * @param channelWrapper
     * @return
     * @throws Throwable
     */
    private Object getReturnObject(Receiver.Wrapper receiverWrapper, ChannelRing.Wrapper channelWrapper) throws Throwable {
        final RmiRequest req = receiverWrapper.getRequest();
        final RmiResponse resp = receiverWrapper.getResponse();
        final Channel channel = channelWrapper.getChannel();
        switch (resp.getCode()) {
            case RESULT_CODE_FAILED_TIMEOUT:
                // 接收到response了，但是response告知已经超时
                throw new TimeoutException(
                        format("received response, but response report provider:%s was timeout. req:%s;resp:%s;",
                                channel.getRemoteAddress(), req, resp));
            case RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW:
                // 接收到response了，但是response告知服务方线程池满
                throw new ThreadPoolOverflowException(
                        format("received response, but response report provider:%s was overflow. req:%s;resp:%s;",
                                channel.getRemoteAddress(), req, resp));
            case RESULT_CODE_FAILED_SERVICE_NOT_FOUND:
                // 接收到response了，但是response告知服务找不到
                throw new ServiceNotFoundException(
                        format("received response, but response report provider:%s was not found the method. req:%s;resp:%s;",
                                channel.getRemoteAddress(), req, resp));
            case RESULT_CODE_SUCCESSED_RETURN:
                // 接收到response了，response报告服务端以return的形式返回
                return resp.getObject();
            case RESULT_CODE_SUCCESSED_THROWABLE:
                // 接收到response了，response报告服务端以抛异常的形式返回
                throw (Throwable) resp.getObject();
            default:
                // 接收到response了，但是不知道response返回的状态码
                throw new UnknowCodeException(
                        format("received response, but response's code is illegal. provider:%s;req:%s;resp:%s;",
                                channel.getRemoteAddress(), req, resp));
        }//case
    }

    /**
     * 参数校验
     *
     * @param targetInterface
     * @param group
     * @param version
     */
    private void check(Class<?> targetInterface, String group, String version) {

        // 检查参数校验
        if (isBlank(version)) {
            throw new IllegalArgumentException("version is blank");
        }
        if (isBlank(group)) {
            throw new IllegalArgumentException("group is blank");
        }
        if (null == targetInterface) {
            throw new IllegalArgumentException("targetInterface is null");
        }

        // 检查传递进来的接口方法中，是否包含有不能序列化的对象类型
        Method[] methods = targetInterface.getMethods();
        if (null != methods) {
            for (Method method : methods) {
                if (!isSerializableType(method.getReturnType())) {
                    throw new IllegalArgumentException("method returnType is not serializable");
                }
                if (!isSerializableType(method.getParameterTypes())) {
                    throw new IllegalArgumentException("method parameter is not serializable");
                }
            }//for
        }//if

    }

    /**
     * 注册服务
     */
    private void registerService(Class<?> targetInterface, String group, String version, long defaultTimeout, Map<String, Long> methodTimeoutMap) {
        final Method[] methods = targetInterface.getMethods();
        if (ArrayUtils.isEmpty(methods)) {
            return;
        }
        for (Method targetMethod : methods) {
            final long timeout = getFixTimeout(targetMethod, defaultTimeout, methodTimeoutMap);
            final String sign = signature(targetMethod);
            final ConsumerService service = new ConsumerService(group, version, sign, timeout, targetInterface, targetMethod);
            messager.post(new SubscribeServiceMessage(service));
        }
    }

    private static volatile ConsumerProxyFactory singleton;

    /**
     * 单例
     *
     * @return
     * @throws Exception
     */
    public static ConsumerProxyFactory singleton() throws Exception {
        if (null == singleton) {
            synchronized (ConsumerProxyFactory.class) {
                if (null == singleton) {
                    singleton = new ConsumerProxyFactory();
                }
            }
        }
        return singleton;
    }

}
