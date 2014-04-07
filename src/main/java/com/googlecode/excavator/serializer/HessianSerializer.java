package com.googlecode.excavator.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

/**
 * hession版的序列化策略
 *
 * @author vlinux
 *
 */
public class HessianSerializer implements Serializer {

    @Override
    public <T extends Serializable> byte[] encode(T serializable)
            throws SerializationException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(os);
        try {
            ho.writeObject(serializable);
        } catch (IOException e) {
            throw new SerializationException(e);
        }
        return os.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T decode(byte[] bytes)
            throws SerializationException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(is);
        try {
            return (T) hi.readObject();
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
