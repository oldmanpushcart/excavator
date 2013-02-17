package com.googlecode.excavator.message;


/**
 * 消息载体
 * @author vlinux
 *
 */
public class Message<T> {

	private final T content;	//消息内容
	private int reTry;			//本消息重试投递次数

	/**
	 * 构造消息载体<br/>
	 * 一个消息必须要有内容
	 * @param t
	 */
	public Message(T t) {
		this.content = t;
		this.reTry = 0;
	}
	
	public T getContent() {
		return content;
	}

	public int getReTry() {
		return reTry;
	}
	
	/**
	 * 每次投递++
	 * @return
	 */
	public Message<T> inc() {
		reTry++;
		return this;
	}
	
}
