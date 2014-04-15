package com.googlecode.excavator.provider.support;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.googlecode.excavator.Supporter;
import com.googlecode.excavator.constant.Log4jConstant;
import com.googlecode.excavator.protocol.RmiRequest;
import com.googlecode.excavator.protocol.coder.ProtocolDecoder;
import com.googlecode.excavator.protocol.coder.ProtocolEncoder;
import com.googlecode.excavator.protocol.coder.RmiDecoder;
import com.googlecode.excavator.protocol.coder.RmiEncoder;
import com.googlecode.excavator.provider.BusinessWorker;

/**
 * 服务server支撑
 *
 * @author vlinux
 *
 */
public class ServerSupport implements Supporter {

    private final Logger logger = Logger.getLogger(Log4jConstant.NETWORK);

    private final InetSocketAddress address;		//提供服务的地址
    private final BusinessWorker businessWorker;	//工作者

    private ServerBootstrap bootstrap;
    private ChannelGroup channelGroup;
    private ChannelPipelineFactory channelPipelineFactory = new ChannelPipelineFactory() {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("protocol-decoder", new ProtocolDecoder());
            pipeline.addLast("rmi-decoder", new RmiDecoder());
            pipeline.addLast("business-handler", businessHandler);
            pipeline.addLast("protocol-encoder", new ProtocolEncoder());
            pipeline.addLast("rmi-encoder", new RmiEncoder());
            return pipeline;
        }

    };

    /*
     * 业务处理器
     */
    private SimpleChannelUpstreamHandler businessHandler = new SimpleChannelUpstreamHandler() {

        @Override
        public void channelConnected(ChannelHandlerContext ctx,
                ChannelStateEvent e) throws Exception {
            super.channelConnected(ctx, e);
            channelGroup.add(ctx.getChannel());
            if (logger.isInfoEnabled()) {
                logger.info(format("client:%s was connected.", ctx.getChannel().getRemoteAddress()));
            }
        }

        @Override
        public void channelDisconnected(ChannelHandlerContext ctx,
                ChannelStateEvent e) throws Exception {
            super.channelDisconnected(ctx, e);
            if (logger.isInfoEnabled()) {
                logger.info(format("client:%s was disconnected.", ctx.getChannel().getRemoteAddress()));
            }
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
                throws Exception {

            if (null == e.getMessage()
                    || !(e.getMessage() instanceof RmiRequest)) {
                super.messageReceived(ctx, e);
            }

            final RmiRequest req = (RmiRequest) e.getMessage();
            businessWorker.work(req, ctx.getChannel());
        }

    };

    
    /**
     * 构造函数
     * @param address
     * @param businessWorker
     */
    public ServerSupport(InetSocketAddress address, BusinessWorker businessWorker) {
        this.address = address;
        this.businessWorker = businessWorker;
    }

    @Override
    public void init() throws Exception {
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                newCachedThreadPool(),
                newCachedThreadPool()));
        bootstrap.setPipelineFactory(channelPipelineFactory);
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        channelGroup = new DefaultChannelGroup();
        channelGroup.add(bootstrap.bind(address));

        if (logger.isInfoEnabled()) {
            logger.info(format("server was started at %s", address));
        }
    }

    @Override
    public void destroy() throws Exception {
        if (null != channelGroup) {
            channelGroup.close().awaitUninterruptibly();
        }
        if (null != bootstrap) {
            bootstrap.releaseExternalResources();
        }
        if (logger.isInfoEnabled()) {
            logger.info("server was shutdown.");
        }
    }

}
