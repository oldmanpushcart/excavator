package com.googlecode.excavator.provider.support;

import static com.netflix.curator.framework.CuratorFrameworkFactory.newClient;
import static com.netflix.curator.framework.state.ConnectionState.RECONNECTED;
import static java.lang.String.format;
import static org.apache.zookeeper.CreateMode.EPHEMERAL;

import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.googlecode.excavator.PropertyConfiger;
import com.googlecode.excavator.Supporter;
import com.googlecode.excavator.constant.Log4jConstant;
import com.googlecode.excavator.message.Message;
import com.googlecode.excavator.message.MessageSubscriber;
import com.googlecode.excavator.message.Messages;
import com.googlecode.excavator.provider.ProviderService;
import com.googlecode.excavator.provider.message.RegisterServiceMessage;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.api.BackgroundCallback;
import com.netflix.curator.framework.api.CuratorEvent;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import com.netflix.curator.retry.ExponentialBackoffRetry;

/**
 * 服务注册支撑
 *
 * @author vlinux
 *
 */
public class ServiceRegisterSupport implements Supporter, MessageSubscriber {

    private final Logger logger = Logger.getLogger(Log4jConstant.ZK);

    private String servers;				//服务器地址列表
    private int connectTimeout;			//连接超时时间
    private int sessionTimeout;			//会话超时时间
    private InetSocketAddress address;	//对外提供服务的网络地址

    private CuratorFramework client;
    private Map<String, ProviderService> services;

    @Override
    public void init() throws Exception {

        // 关注注册服务消息
        Messages.register(this, RegisterServiceMessage.class);

        services = Maps.newConcurrentMap();

        client = newClient(
                servers,
                sessionTimeout,
                connectTimeout,
                new ExponentialBackoffRetry(500, 20));

        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == RECONNECTED) {
                    if (logger.isInfoEnabled()) {
                        logger.info("zk-server reconnected, must reRegister right now.");
                    }
                    for (ProviderService providerService : services.values()) {
                        Messages.post(new RegisterServiceMessage(providerService));
                    }
                }
            }

        });
        client.start();

    }

    @Override
    public void receive(Message<?> msg) throws Exception {

        if (!(msg instanceof RegisterServiceMessage)) {
            return;
        }

        final ProviderService service = ((RegisterServiceMessage) msg).getContent();
        final String key = service.getKey();

        if (services.containsKey(key)) {
            if (logger.isInfoEnabled()) {
                logger.info(format("service:%s already registed.", service));
            }
        }

        final String pref = format("/excavator/nondurable/%s/%s/%s",
                service.getGroup(),
                service.getVersion(),
                service.getSign());

        try {
            if (client.checkExists().forPath(pref) == null) {
                client.create().creatingParentsIfNeeded().forPath(pref);
            }
        } catch (Exception e) {
            //do nothing...
        }

        try {
            client.create().withMode(EPHEMERAL).inBackground(new BackgroundCallback() {

                @Override
                public void processResult(CuratorFramework client, CuratorEvent event)
                        throws Exception {
                    services.put(key, service);
                    if (logger.isInfoEnabled()) {
                        logger.info(format("register service:%s successed.", service));
                    }

                }

            }).forPath(format("%s/%s:%s:%s",
                    pref,
                    address.getAddress().getHostAddress(),
                    address.getPort(),
                    PropertyConfiger.getAppName()));
        } catch (Exception e) {
            logger.warn(format("create service:%s path failed", service), e);
        }

    }

    @Override
    public void destroy() throws Exception {
        if (null != client) {
            client.close();
        }
    }

    public void setServers(String servers) {
        this.servers = servers;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

}
