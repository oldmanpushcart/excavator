package com.googlecode.excavator.protocol.coder;

import static com.googlecode.excavator.protocol.Protocol.TYPE_RMI;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.googlecode.excavator.protocol.Protocol;
import com.googlecode.excavator.protocol.RmiTracer;
import com.googlecode.excavator.serializer.Serializer;
import com.googlecode.excavator.serializer.SerializerFactory;

/**
 * rmi–≠“È±‡¬Î∆˜
 *
 * @author vlinux
 *
 */
public class RmiEncoder extends OneToOneEncoder {

    private Serializer serializer = SerializerFactory.getInstance();

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel,
            Object msg) throws Exception {

        RmiTracer rmi = (RmiTracer) msg;
        Protocol pro = new Protocol();
        pro.setType(TYPE_RMI);
        byte[] datas = serializer.encode(rmi);
        pro.setLength(datas.length);
        pro.setDatas(datas);

        return pro;
    }

}
