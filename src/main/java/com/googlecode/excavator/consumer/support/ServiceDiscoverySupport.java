package com.googlecode.excavator.consumer.support;

import static com.googlecode.excavator.consumer.message.ChannelChangedMessage.Type.CREATE;
import static com.googlecode.excavator.consumer.message.ChannelChangedMessage.Type.REMOVE;
import static com.netflix.curator.framework.CuratorFrameworkFactory.newClient;
import static java.lang.String.format;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.googlecode.excavator.Supporter;
import com.googlecode.excavator.constant.Log4jConstant;
import com.googlecode.excavator.consumer.ConsumerService;
import com.googlecode.excavator.consumer.message.ChannelChangedMessage;
import com.googlecode.excavator.consumer.message.SubscribeServiceMessage;
import com.googlecode.excavator.message.Message;
import com.googlecode.excavator.message.MessageSubscriber;
import com.googlecode.excavator.message.Messager;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.cache.PathChildrenCache;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheEvent;
import com.netflix.curator.framework.recipes.cache.PathChildrenCacheListener;
import com.netflix.curator.retry.ExponentialBackoffRetry;

/**
 * 服务发现支撑
 * @author vlinux
 *
 */
public class ServiceDiscoverySupport implements Supporter, MessageSubscriber {

    private final Logger logger = Logger.getLogger(Log4jConstant.ZK);

    private final String zkServerLists;	//服务器地址列表
    private final int zkConnectTimeout;	//连接超时时间
    private final int zkSessionTimeout;	//会话超时时间
    private final Messager messager;
    
    private CuratorFramework client;
    private Map<String, ConsumerService> services;
    private List<PathChildrenCache> pathChildrenCaches = new ArrayList<PathChildrenCache>();

    /**
     * 构造函数
     * @param zkServerLists
     * @param zkConnectTimeout
     * @param zkSessionTimeout
     * @param messager
     */
    public ServiceDiscoverySupport(String zkServerLists, int zkConnectTimeout, int zkSessionTimeout,
            Messager messager) {
        this.zkServerLists = zkServerLists;
        this.zkConnectTimeout = zkConnectTimeout;
        this.zkSessionTimeout = zkSessionTimeout;
        this.messager = messager;
    }

    private PathChildrenCacheListener listener = new PathChildrenCacheListener() {

        @Override
        public void childEvent(CuratorFramework client,
                PathChildrenCacheEvent event) throws Exception {

            switch (event.getType()) {
                case CHILD_ADDED:
                    postChannelChangedMessage(event, CREATE);
                    if (logger.isInfoEnabled()) {
                        logger.info(format("receive service changed[create]. evnet=%s", event));
                    }
                    break;
                case CHILD_REMOVED:
                    postChannelChangedMessage(event, REMOVE);
                    if (logger.isInfoEnabled()) {
                        logger.info(format("receive service changed[remove]. evnet=%s", event));
                    }
                    break;
                default:
                    if (logger.isInfoEnabled()) {
                        logger.info(format("receive an unknow type event, ignore it. event=%s", event));
                    }
            }
        }

    };

    /**
     * 投递链接变更消息
     *
     * @param event
     * @param type
     */
    private void postChannelChangedMessage(PathChildrenCacheEvent event, ChannelChangedMessage.Type type) {
        //"/excavator/nondurable/G1/1.0.0/176349878f5a1bb7df5b61741d981d35/127.0.0.1:3658";
        final String[] strs = event.getData().getPath().split("/");
        final String key = format("%s%s%s", strs[3]/*group*/, strs[4]/*version*/, strs[5]/*sign*/);
        final String[] addressStrs = strs[6].split(":");
        final InetSocketAddress address = new InetSocketAddress(addressStrs[0], Integer.valueOf(addressStrs[1]));
        final String provider = addressStrs[2];
        final ConsumerService service = services.get(key);
        messager.post(new ChannelChangedMessage(service, provider, address, type));
    }

    @Override
    public void init() throws Exception {
        messager.register(this, SubscribeServiceMessage.class);
        services = Maps.newConcurrentMap();
        client = newClient(
                zkServerLists,
                zkSessionTimeout,
                zkConnectTimeout,
                new ExponentialBackoffRetry(500, 20));
        client.start();
    }

    @Override
    public void destroy() throws Exception {
        for (PathChildrenCache pathChildrenCache : pathChildrenCaches) {
            pathChildrenCache.close();
        }
        if (null != client) {
            client.close();
        }
    }

    @Override
    public void receive(Message<?> msg) throws Exception {

        if (!(msg instanceof SubscribeServiceMessage)) {
            return;
        }

        final SubscribeServiceMessage ssMsg = (SubscribeServiceMessage) msg;
        final ConsumerService service = ssMsg.getContent();
        final String pref = format("/excavator/nondurable/%s/%s/%s",
                service.getGroup(),
                service.getVersion(),
                service.getSign());

        final PathChildrenCache pathCache = new PathChildrenCache(client, pref, false);
        pathChildrenCaches.add(pathCache);
        try {
            pathCache.getListenable().addListener(listener);
            pathCache.start();
            services.put(service.getKey(), service);
        } catch (Exception e) {
            logger.warn(format("subscribe %s was failed", pref), e);
        } finally {
            pathCache.close();
        }

    }

}
