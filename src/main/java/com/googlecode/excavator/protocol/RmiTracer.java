package com.googlecode.excavator.protocol;

import static java.lang.System.currentTimeMillis;

import java.io.Serializable;

/**
 * rmi调用跟踪类<br/>
 * 此类是rmi request/response 的基类，主要用于负责产生id和token，以便跟踪
 *
 * @author vlinux
 *
 */
public class RmiTracer implements Serializable {

    private static final long serialVersionUID = -940954284480781971L;

//    /**
//     * rmi的递增序列
//     */
//    private static transient final AtomicLong seq = new AtomicLong();

//    private final long id;
    private String token;
    private long timestamp = currentTimeMillis();

//    /**
//     * 携带id和token的构造函数<br/>
//     * 用于rmi应答类构造函数的场景
//     *
//     * @param id
//     * @param token
//     */
//    public RmiTracer(long id, String token) {
//        this.id = id;
//        this.token = token;
//        this.timestamp = currentTimeMillis();
//    }

//    /**
//     * 不携带id与token的构造函数<br/>
//     * id将会自增,token将会随机生成<br/>
//     * 用于最初发起rmi请求的场景
//     */
//    public RmiTracer() {
//        this.token = isEnableToken()
//                ? StringUtils.EMPTY
//                : UUID.randomUUID().toString();
//    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
