package com.googlecode.excavator.util;

import java.io.Serializable;

/**
 * 序列化工具
 *
 * @author vlinux
 *
 */
public final class SerializerUtil {

    /**
     * 判断一个类型是否可序列化
     *
     * @param type
     * @return
     */
    public static boolean isSerializableType(Class<?> type) {

        // void 是可序列化的
        if (Void.class.isAssignableFrom(type)) {
            return true;
        }

        // 如果type是Object的子类，则必须要求其实现Serializable接口
        if (Object.class.isAssignableFrom(type)
                && !Serializable.class.isAssignableFrom(type)) {
            return false;
        }

        // 其他情况八种基本类型、和已经实现了序列化接口的Object对象，则都是可序列化的
        return true;
    }

    /**
     * 判断一堆类型是否可序列化
     *
     * @param types
     * @return
     */
    public static boolean isSerializableType(Class<?>... types) {

        // 如果传递的列表为空，空集是可以进行序列化的
        if (null == types
                || types.length == 0) {
            return true;
        }

        for (Class<?> type : types) {
            if (!isSerializableType(type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将传入的转换为序列化类型
     *
     * @param args
     * @return
     */
    public static Serializable[] changeToSerializable(Object[] args) {
        if (null == args) {
            return null;
        }
        Serializable[] serializables = new Serializable[args.length];
        for (int index = 0; index < args.length; index++) {
            serializables[index] = (Serializable) args[index];
        }
        return serializables;
    }

}
