package com.googlecode.excavator.util;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 签名工具类
 *
 * @author vlinux
 *
 */
public final class SignatureUtil {

    /*
     * 方法签名缓存
     */
    private static Map<Method, String> methodSignCache = Maps.newConcurrentMap();

    /**
     * 获取当前方法的服务签名
     *
     * @param method
     * @return
     */
    public static String signature(Method method) {

        if (methodSignCache.containsKey(method)) {
            return methodSignCache.get(method);
        }

        final StringBuilder paramTypeStrSB = new StringBuilder();
        final Class<?>[] paramTypes = method.getParameterTypes();
        if (null != paramTypes
                && paramTypes.length != 0) {
            for (int index = 0; index < paramTypes.length; index++) {
                paramTypeStrSB.append(paramTypes[index].getName());
                if (index != paramTypes.length - 1) {
                    paramTypeStrSB.append(",");
                }
            }//for
        }//if

        final String signStr = String.format("%s %s.%s(%s)",
                method.getReturnType().getName(),
                method.getDeclaringClass().getName(),
                method.getName(),
                paramTypeStrSB.toString());

        String signMD5 = md5(signStr);
        methodSignCache.put(method, signMD5);
        return signMD5;
    }

    /**
     * 根据一个字符串计算出MD5值
     *
     * @param str
     * @return
     */
    public static String md5(String str) {
        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes());
        } catch (NoSuchAlgorithmException e) {
            return org.apache.commons.lang.StringUtils.EMPTY;
        }

        byte[] byteArray = messageDigest.digest();

        StringBuilder md5SB = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5SB.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5SB.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }//for

        return md5SB.toString();
    }

}
