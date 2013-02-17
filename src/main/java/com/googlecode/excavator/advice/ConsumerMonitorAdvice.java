package com.googlecode.excavator.advice;

import com.googlecode.excavator.monitor.Monitor.Type;

@Direction(types=Direction.Type.CONSUMER)
public class ConsumerMonitorAdvice extends MonitorAdvice {

	@Override
	public Type getType() {
		return Type.CONSUMER;
	}

}
