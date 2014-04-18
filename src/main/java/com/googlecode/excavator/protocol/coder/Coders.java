package com.googlecode.excavator.protocol.coder;

import static com.googlecode.excavator.protocol.Protocol.TYPE_RMI;

import com.googlecode.excavator.exception.ProtocolCoderException;
import com.googlecode.excavator.protocol.Protocol;
import com.googlecode.excavator.protocol.RmiRequest;
import com.googlecode.excavator.protocol.RmiResponse;
import com.googlecode.excavator.protocol.RmiTracer;
import com.googlecode.excavator.serializer.SerializationException;
import com.googlecode.excavator.serializer.Serializer;
import com.googlecode.excavator.serializer.SerializerFactory;

/**
 * 编码解码工具类<br/>
 * 在将Rmi的序列化从netty工作线程中剥离之后，这个部分的功能就只能从Rmi(De/En)conder功能中抽出来，成为工具类了
 * @author vlinux
 *
 */
public class Coders {

    private static Serializer serializer = SerializerFactory.getInstance();
    
    /**
     * 将rmi转换为协议:请求
     * @param rmi
     * @return
     * @throws ProtocolCoderException
     */
    public static Protocol toProtocol(RmiRequest rmi) throws ProtocolCoderException {
        try {
            Protocol pro = new Protocol();
            pro.setType(TYPE_RMI);
            byte[] datas = serializer.encode(rmi);
            pro.setLength(datas.length);
            pro.setDatas(datas);
            return pro;
        } catch (SerializationException e) {
            throw new ProtocolCoderException("toProtocol:request failed.", e);
        }
    }
    
    /**
     * 将rmi转换为协议:应答
     * @param id
     * @param rmi
     * @return
     * @throws ProtocolCoderException
     */
    public static Protocol toProtocol(int id, RmiResponse rmi) throws ProtocolCoderException {
        try {
            Protocol pro = new Protocol();
            pro.setId(id);
            pro.setType(TYPE_RMI);
            byte[] datas = serializer.encode(rmi);
            pro.setLength(datas.length);
            pro.setDatas(datas);
            return pro;
        } catch (SerializationException e) {
            throw new ProtocolCoderException("toProtocol:response failed.", e);
        }
    }
    
    /**
     * 将proto协议转换为RmiTracer
     * @param pro
     * @return
     * @throws ProtocolCoderException
     */
    public static RmiTracer toRmiTracer(Protocol pro) throws ProtocolCoderException {
        if (pro.getType() != TYPE_RMI) {
            throw new ProtocolCoderException("toRmiTracer failed, beacuse pro.type was not RMI");
        }
        try {
            return serializer.decode(pro.getDatas());
        }
        catch (SerializationException e) {
            throw new ProtocolCoderException("toRmiTracer failed.", e);
        }
    }
    
}
