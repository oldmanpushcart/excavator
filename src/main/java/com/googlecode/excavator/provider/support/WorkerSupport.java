package com.googlecode.excavator.provider.support;

import static com.googlecode.excavator.PropertyConfiger.getAppName;
import static com.googlecode.excavator.advice.Advices.doAfter;
import static com.googlecode.excavator.advice.Advices.doBefores;
import static com.googlecode.excavator.advice.Advices.doFinally;
import static com.googlecode.excavator.advice.Advices.doThrow;
import static com.googlecode.excavator.advice.Direction.Type.PROVIDER;
import static com.googlecode.excavator.protocol.Protocol.TYPE_RMI;
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
import com.googlecode.excavator.protocol.Protocol;
import com.googlecode.excavator.protocol.RmiRequest;
import com.googlecode.excavator.protocol.RmiResponse;
import com.googlecode.excavator.protocol.RmiTracer;
import com.googlecode.excavator.provider.BusinessWorker;
import com.googlecode.excavator.provider.ProviderService;
import com.googlecode.excavator.provider.message.RegisterServiceMessage;
import com.googlecode.excavator.serializer.SerializationException;
import com.googlecode.excavator.serializer.Serializer;
import com.googlecode.excavator.serializer.SerializerFactory;

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
    private final Serializer serializer = SerializerFactory.getInstance();
    
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
    public void work(final Protocol pro, final Channel channel) {

        businessExecutor.execute(new Runnable() {

            /**
             * 将协议对象转换为请求对象
             * @param pro
             * @return
             */
            private RmiRequest convertToReq(Protocol pro) {
                if (pro.getType() != TYPE_RMI) {
                    logger.warn("ingore this request, because proto.type was {}, need TYPE_RMI. remote={};", pro.getType(), channel.getRemoteAddress());
                    return null;
                }
                final RmiTracer rmiTracer;
                try {
                    rmiTracer = serializer.decode(pro.getDatas());
                } catch (SerializationException e) {
                    logger.warn("ingore this request, because decode failed. remote={}", channel.getRemoteAddress(), e);
                    return null;
                }
                if( null == rmiTracer ) {
                    // 这里应该不可能走到
                    logger.warn("ingore this request, because it was null.");
                    return null;
                }
                if( !(rmiTracer instanceof RmiRequest) ) {
                    logger.warn("ingore this request, because rmiTracer's class was {}, need RmiRequest. remote={};", 
                        rmiTracer.getClass().getSimpleName(), 
                        channel.getRemoteAddress());
                    return null;
                }
                return (RmiRequest)rmiTracer;
            }
            
            @Override
            public void run() {
                
                final RmiRequest req = convertToReq(pro);
                if( null == req ) {
                    logger.warn("convertToReq failed. remote={};", channel.getRemoteAddress());
                    // 对于协议类型的出错，我们只能很遗憾的打印一个log，然后等待客户端超时
                    return;
                }
                
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
                
                doWork(req, channel, service);

            }

        });

    }

    /**
     * 反序列化并写入返回信息
     * @param resp
     * @param channel
     */
    private void writeResponse(RmiResponse resp, Channel channel) {
        try {
            RmiTracer rmi = (RmiTracer) resp;
            Protocol pro = new Protocol();
            pro.setType(TYPE_RMI);
            byte[] datas = serializer.encode(rmi);
            pro.setLength(datas.length);
            pro.setDatas(datas);
            channel.write(pro);
        } catch (SerializationException e) {
            logger.warn("write response failed, because serializer failed. resp={};remote={};",
                new Object[]{resp,channel.getRemoteAddress(),e});
        }
        
    }
    
    /**
     * 处理请求服务不存在
     *
     * @param req
     * @param channel
     */
    private void handleServiceNotFound(RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_FAILED_SERVICE_NOT_FOUND);
        writeResponse(respEvt, channel);
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
        writeResponse(respEvt, channel);
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
        writeResponse(respEvt, channel);
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
        writeResponse(respEvt, channel);
    }

    /**
     * 处理线程池满异常
     *
     * @param req
     * @param channel
     */
    private void handleOverflow(RmiRequest req, Channel channel) {
        RmiResponse respEvt = new RmiResponse(req, RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW);
        writeResponse(respEvt, channel);
    }

}
