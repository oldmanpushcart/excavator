package com.googlecode.excavator.advice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记通知的方向
 *
 * @author vlinux
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Direction {

    /**
     * 通知方向
     *
     * @author vlinux
     *
     */
    public static enum Type {

        /**
         * 客户端
         */
        CONSUMER,
        /**
         * 服务端
         */
        PROVIDER

    }

    /**
     * 通知方向
     *
     * @return
     */
    Type[] types() default {Type.CONSUMER, Type.PROVIDER};

}
