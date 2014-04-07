package com.googlecode.excavator.advice;

import static com.googlecode.excavator.PropertyConfiger.getProfilerLimit;
import static com.googlecode.excavator.PropertyConfiger.isEnableProfiler;

import org.apache.log4j.Logger;

import com.googlecode.excavator.Profiler;
import com.googlecode.excavator.PropertyConfiger;
import com.googlecode.excavator.Runtimes.Runtime;
import com.googlecode.excavator.advice.Advices.Advice;
import com.googlecode.excavator.constant.Log4jConstant;

/**
 * 性能点通知
 *
 * @author vlinux
 *
 */
@Direction
public class ProfilerAdvice implements Advice {

    private final Logger logger = Logger.getLogger(Log4jConstant.PROFILER);

    @Override
    public void doBefore(Runtime runtime) throws Throwable {
        if (isEnableProfiler()) {
            Profiler.start("profilter:" + runtime.getServiceInterface().getSimpleName() + "$" + runtime.getServiceMtd().getName());
        }
    }

    @Override
    public void doAfter(Runtime runtime, Object returnObj, long cost) {
        if (isEnableProfiler()) {
            final long timeout = runtime.getReq().getTimeout();
            if (timeout - cost <= getProfilerLimit()) {
                // 如果快接近超时时间(timeout超过一半)
                final String dump = Profiler.dump() + "\n";
                logger.warn(dump);
            }
        }
    }

    @Override
    public void doThrow(Runtime runtime, Throwable throwable) {
		// TODO Auto-generated method stub

    }

    @Override
    public void doFinally(Runtime runtime) {
        if (PropertyConfiger.isEnableProfiler()) {
            Profiler.release();
        }
    }

}
