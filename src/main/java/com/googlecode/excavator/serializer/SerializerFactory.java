package com.googlecode.excavator.serializer;

import static com.googlecode.excavator.PropertyConfiger.getSerializerName;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 序列化工厂
 *
 * @author vlinux
 *
 */
public class SerializerFactory {

    private static final Map<String, Serializer> serializers = Maps.newConcurrentMap();

    /**
     * Java序列化方式
     */
    public static final String SERIALIZER_NAME_JAVA = "java";

    /**
     * hessian序列化方式
     */
    public static final String SERIALIZER_NAME_HESSIAN = "hessian";

    /**
     * 注册序列化方式
     *
     * @param name
     * @param serializer
     */
    public static void register(String name, Serializer serializer) {
        serializers.put(name, serializer);
    }

    static {

        // 注册java序列化的方式
        register(SERIALIZER_NAME_JAVA, new JavaSerializer());

        // 注册hessian序列化的方式
        register(SERIALIZER_NAME_HESSIAN, new HessianSerializer());

    }

    /**
     * 获取序列化解析器
     *
     * @return
     */
    public static Serializer getInstance() {
        final String name = getSerializerName();
        if (serializers.containsKey(name)) {
            return serializers.get(name);
        }
        return serializers.get(SERIALIZER_NAME_HESSIAN);
    }

}
