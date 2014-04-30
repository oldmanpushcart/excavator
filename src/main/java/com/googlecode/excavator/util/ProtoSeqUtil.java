package com.googlecode.excavator.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 协议序列生成器
 * @author vlinux
 *
 */
public class ProtoSeqUtil {

    /**
     * rmi的递增序列
     */
    private static final AtomicLong seq = new AtomicLong();
    
    /**
     * 序列发生器
     * @return
     */
    public static long seq() {
        return seq.incrementAndGet();
    }
    
}
