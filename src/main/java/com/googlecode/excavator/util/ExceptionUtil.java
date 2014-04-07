package com.googlecode.excavator.util;

import java.io.IOException;

/**
 * 异常工具类
 *
 * @author vlinux
 *
 */
public final class ExceptionUtil {

    /**
     * 检测抛出的异常中是否包含了网络通讯异常
     *
     * @param t
     * @return
     */
    public static boolean hasNetworkException(Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof IOException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}
