package com.googlecode.excavator.advice;

import com.googlecode.excavator.Runtimes;
import com.googlecode.excavator.Runtimes.Runtime;
import com.googlecode.excavator.advice.Advices.Advice;

/**
 * 运行环境通知
 * @author vlinux
 *
 */
@Direction
public class RuntimeAdvice implements Advice {

	@Override
	public void doBefore(Runtime runtime) throws Throwable {
		Runtimes.inject(runtime);
	}

	@Override
	public void doAfter(Runtime runtime, Object returnObj, long cost) {
		Runtimes.remove();
	}

	@Override
	public void doThrow(Runtime runtime, Throwable throwable) {
		Runtimes.remove();
	}

	@Override
	public void doFinally(Runtime runtime) {
		// TODO Auto-generated method stub
		
	}

}
