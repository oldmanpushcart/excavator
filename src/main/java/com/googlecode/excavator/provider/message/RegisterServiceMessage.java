package com.googlecode.excavator.provider.message;

import com.googlecode.excavator.message.Message;
import com.googlecode.excavator.provider.ProviderService;

/**
 * 服务注册消息<br/>
 * 收到这条消息表明收到了需要想服务中心注册消息的通知
 * @author vlinux
 *
 */
public class RegisterServiceMessage extends Message<ProviderService> {

	public RegisterServiceMessage(ProviderService t) {
		super(t);
	}

}
