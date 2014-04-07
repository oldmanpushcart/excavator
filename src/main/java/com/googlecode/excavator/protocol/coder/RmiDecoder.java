package com.googlecode.excavator.protocol.coder;

import static com.googlecode.excavator.protocol.Protocol.TYPE_RMI;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

import com.googlecode.excavator.protocol.Protocol;
import com.googlecode.excavator.protocol.RmiTracer;
import com.googlecode.excavator.serializer.Serializer;
import com.googlecode.excavator.serializer.SerializerFactory;

/**
 * rmi–≠“ÈΩ‚¬Î∆˜
 *
 * @author vlinux
 *
 */
public class RmiDecoder extends OneToOneDecoder {

    private Serializer serializer = SerializerFactory.getInstance();

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
            Object msg) throws Exception {
        Protocol pro = (Protocol) msg;
        if (pro.getType() != TYPE_RMI) {
            return null;
        }
        RmiTracer rmiTracer = serializer.decode(pro.getDatas());
        return rmiTracer;
    }

}
