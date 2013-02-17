package com.googlecode.excavator.advice;

import com.googlecode.excavator.monitor.Monitor.Type;

@Direction(types=Direction.Type.PROVIDER)
public class ProviderMonitorAdvice extends MonitorAdvice {

	@Override
	public Type getType() {
		return Type.PROVIDER;
	}

}
