package com.googlecode.excavator.advice;

import static com.googlecode.excavator.PropertyConfiger.getAppName;
import static com.googlecode.excavator.PropertyConfiger.isEnableMonitor;
import static com.googlecode.excavator.monitor.Monitors.monitor;

import com.googlecode.excavator.Runtimes.Runtime;
import com.googlecode.excavator.advice.Advices.Advice;
import com.googlecode.excavator.monitor.Monitor;
import com.googlecode.excavator.protocol.RmiRequest;

/**
 * 监控通知抽象类
 *
 * @author vlinux
 *
 */
public abstract class MonitorAdvice implements Advice {

    @Override
    public void doBefore(Runtime runtime) throws Throwable {
		// TODO Auto-generated method stub

    }

    @Override
    public void doAfter(Runtime runtime, Object returnObj, long cost) {
        // 记录monitor
        if (isEnableMonitor()) {
            final RmiRequest req = runtime.getReq();

            // 记录日志
            monitor(getType(),
                    req.getGroup(),
                    req.getVersion(),
                    req.getSign(),
                    req.getAppName(),
                    getAppName(),
                    cost);
        }
    }

    public abstract Monitor.Type getType();

    @Override
    public void doThrow(Runtime runtime, Throwable throwable) {
		// TODO Auto-generated method stub

    }

    @Override
    public void doFinally(Runtime runtime) {
		// TODO Auto-generated method stub

    }

}
