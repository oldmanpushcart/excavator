package com.googlecode.excavator.provider.support;

import static com.googlecode.excavator.PropertyConfiger.getAppName;
import static com.googlecode.excavator.advice.Advices.doAfter;
import static com.googlecode.excavator.advice.Advices.doBefores;
import static com.googlecode.excavator.advice.Advices.doFinally;
import static com.googlecode.excavator.advice.Advices.doThrow;
import static com.googlecode.excavator.advice.Direction.Type.PROVIDER;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_SERVICE_NOT_FOUND;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_FAILED_TIMEOUT;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_SUCCESSED_RETURN;
import static com.googlecode.excavator.protocol.RmiResponse.RESULT_CODE_SUCCESSED_THROWABLE;
import static java.lang.System.currentTimeMillis;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.googlecode.excavator.Runtimes.Runtime;
import com.googlecode.excavator.Supporter;
import com.googlecode.excavator.constant.LogConstant;
import com.googlecode.excavator.message.Message;
import com.googlecode.excavator.message.MessageSubscriber;
import com.googlecode.excavator.message.Messager;
import com.googlecode.excavator.protocol.RmiRequest;
import com.googlecode.excavator.protocol.RmiResponse;
import com.googlecode.excavator.provider.BusinessWorker;
import com.googlecode.excavator.provider.ProviderService;
import com.googlecode.excavator.provider.message.RegisterServiceMessage;

/**
 * 工作线程支撑
 *
 * @author vlinux
 *
 */
public class WorkerSupport implements Supporter, MessageSubscriber,
        BusinessWorker {

    private final Logger logger = LoggerFactory.getLogger(LogConstant.WORKER);

    private final Messager messager;
    private final int poolSize; 					// 业务执行线程数量
    
    private ExecutorService businessExecutor; 		// 业务执行者线程
    private Semaphore semaphore; 					// 流控信号量
    private Map<String, ProviderService> services; 	// 服务列表

    /**
     * 构造函数
     * @param messager
     * @param poolSize
     */
    public WorkerSupport(Messager messager, int poolSize) {
        this.messager = messager;
        this.poolSize = poolSize;
    }

    @Override
    public void init() throws Exception {

        messager.register(this, RegisterServiceMessage.class);

        // 初始化服务列表
        services = Maps.newConcurrentMap();

        // 执行线程池
        businessExecutor = Executors.newCachedThreadPool(new ThreadFactory() {

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "excavator-biz-worker");
            }

        });

        // 初始化信号量
        semaphore = new Semaphore(poolSize);

        logger.info("worker init thread_pool size:{}", poolSize);

    }

    @Override
    public void destroy() throws Exception {
        if (null != businessExecutor) {
            businessExecutor.shutdown();
        }
    }

    @Override
    public void receive(Message<?> msg) throws Exception {

        if (!(msg instanceof RegisterServiceMessage)) {
            return;
        }

        RegisterServiceMessage rsMsg = (RegisterServiceMessage) msg;
        ProviderService service = rsMsg.getContent();
        services.put(service.getKey(), service);

    }

    /**
     * 在这里执行具体的调用
     *
     * @param req
     * @param channel
     * @param service
     */
    private void doWork(RmiRequest req, Channel channel, ProviderService service) {
        final long start = currentTimeMillis();
        final Object serviceObj = service.getServiceObject();
        final Method serviceMtd = service.getServiceMethod();

        // 生成运行时环境
        final Runtime runtime = new Runtime(req, getAppName(),
                service.getServiceItf(), serviceMtd,
                (InetSocketAddress) channel.getRemoteAddress(),
                (InetSocketAddress) channel.getLocalAddress());

        // 干活！
        try {

            doBefores(PROVIDER, runtime);

            try {
                final Serializable returnObj = (Serializable) serviceMtd.invoke(serviceObj, (Object[]) req.getArgs());
                doAfter(PROVIDER, runtime, returnObj, currentTimeMillis() - start/*cost*/);
                handleNormal(returnObj, req, channel);
            } catch (Throwable t) {
                throw t.getCause().getCause().getCause();
            }

        } catch (Throwable t) {
            doThrow(PROVIDER, runtime, t);
            handleThrowable(t, req, channel);
        } finally {
            semaphore.release();
            doFinally(PROVIDER, runtime);
        }//try
    }

    @Override
    public void work(final RmiRequest req, final Channel channel) {

        final String key = req.getKey();

        // 服务不存在
        final ProviderService service = services.get(key);
        if (null == service) {
            handleServiceNotFound(req, channel);
            return;
        }

        // 如果超时了，则走处理请求超时
        if (isReqTimeout(req.getTimestamp(), req.getTimeout(), service.getTimeout())) {
            handleTimeout(req, channel);
            return;
        }

        // 线程数量控制
        if (!semaphore.tryAcquire()) {
            handleOverflow(req, channel);
            return;
        }

        businessExecutor.execute(new Runnable() {

            @Override
            public void run() {

                doWork(req, channel, service);

            }

        });

    }

    /**
     * 处理请求服务不存在
     *
     * @param req
     * @param channel
     */
    private void handleServiceNotFound(RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_FAILED_SERVICE_NOT_FOUND);
        channel.write(respEvt);
    }

    /**
     * 请求是否已经超时
     *
     * @param reqTimestamp
     * @param reqTimeout
     * @param proTimeout
     * @return
     */
    private final boolean isReqTimeout(long reqTimestamp, long reqTimeout, long proTimeout) {
        final long nowTimestamp = System.currentTimeMillis();
        return nowTimestamp - reqTimestamp > Math.min(reqTimeout, proTimeout);
    }

    /**
     * 处理超时情况
     *
     * @param req
     * @param channel
     */
    private void handleTimeout(RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_FAILED_TIMEOUT);
        channel.write(respEvt);
    }

    /**
     * 处理正常return返回的情况
     *
     * @param returnObj
     * @param req
     * @param channel
     */
    private final void handleNormal(Serializable returnObj, RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_SUCCESSED_RETURN, returnObj);
        channel.write(respEvt);
    }

    /**
     * 处理以抛异常返回的情况
     *
     * @param returnObj
     * @param req
     * @param channel
     */
    private void handleThrowable(Serializable returnObj, RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_SUCCESSED_THROWABLE, returnObj);
        channel.write(respEvt);
    }

    /**
     * 处理线程池满异常
     *
     * @param req
     * @param channel
     */
    private void handleOverflow(RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW);
        channel.write(respEvt);
    }

}
