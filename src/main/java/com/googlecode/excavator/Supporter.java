package com.googlecode.excavator;

/**
 * 支撑者
 *
 * @author vlinux
 *
 */
public interface Supporter {

    /**
     * 初始化
     *
     * @throws Exception
     */
    void init() throws Exception;

    /**
     * 销毁
     *
     * @throws Exception
     */
    void destroy() throws Exception;

}
