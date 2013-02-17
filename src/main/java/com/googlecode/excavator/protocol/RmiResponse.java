package com.googlecode.excavator.protocol;

import java.io.Serializable;

/**
 * rmi调用协议:应答
 * @author vlinux
 *
 */
public final class RmiResponse extends RmiTracer {

	private static final long serialVersionUID = -2335302314388825625L;

	/**
	 * 返回结果码：调用成功，以正常方式返回结果
	 */
	public static final short RESULT_CODE_SUCCESSED_RETURN = 1;
	
	/**
	 * 返回结果码：调用成功，以抛异常方式返回结果
	 */
	public static final short RESULT_CODE_SUCCESSED_THROWABLE = 2;
	
	/**
	 * 返回结果码：调用失败，服务方线程池已满
	 */
	public static final short RESULT_CODE_FAILED_BIZ_THREAD_POOL_OVERFLOW = 4;
	
	/**
	 * 返回结果码：调用失败，服务不存在
	 */
	public static final short RESULT_CODE_FAILED_SERVICE_NOT_FOUND = 5;
	
	/**
	 * 返回结果码：调用失败，服务超时
	 */
	public static final short RESULT_CODE_FAILED_TIMEOUT = 6;
	
	private final short code;			//返回结果码
	private final Serializable object;	//返回结果
	
	/**
	 * 构造rmi应答
	 * @param req 应答所对应的请求<br/>
	 * 	此时构造出来的应答包将包含有与请求相同的id和token
	 * @param code
	 * @param object
	 */
	public RmiResponse(RmiRequest req, short code, Serializable object) {
		super(req.getId(), req.getToken());
		this.code = code;
		this.object = object;
	}
	
	/**
	 * 构造rmi应答<br/>
	 * 无返回值版本，用于构造出错场景的rmi应答,此时不需要有回复值
	 * @param req
	 * @param code
	 */
	public RmiResponse(RmiRequest req, short code) {
		super(req.getId(), req.getToken());
		this.code = code;
		this.object = null;
	}

	public short getCode() {
		return code;
	}

	public Serializable getObject() {
		return object;
	}
	
	@Override
	public String toString() {
		return String.format("RESP[id=%s;token=%s;code=%s;]", getId(), getToken(), code );
	}
	
}
