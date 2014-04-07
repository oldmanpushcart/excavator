package com.googlecode.excavator.advice;

import java.util.Set;

import com.google.common.collect.Sets;
import com.googlecode.excavator.Runtimes;

/**
 * 通知
 *
 * @author vlinux
 *
 */
public class Advices {

    /**
     * 通知点
     *
     * @author vlinux
     *
     */
    public static interface Advice {

        /**
         * 前置通知
         *
         * @param runtime
         * @throws Throwable
         */
        void doBefore(Runtimes.Runtime runtime) throws Throwable;

        /**
         * 后置通知
         *
         * @param runtime
         * @param returnObj
         * @param cost
         * @throws Throwable
         */
        void doAfter(Runtimes.Runtime runtime, Object returnObj, long cost);

        /**
         * 异常通知
         *
         * @param runtime
         * @param throwable
         */
        void doThrow(Runtimes.Runtime runtime, Throwable throwable);

        /**
         * 完成通知
         *
         * @param runtime
         */
        void doFinally(Runtimes.Runtime runtime);

    }

    private static final Set<Advice> consumerAdvices = Sets.newLinkedHashSet();
    private static final Set<Advice> providerAdvices = Sets.newLinkedHashSet();

    /**
     * 注册一个通知点
     *
     * @param advice
     */
    public static void register(Advice advice) {

        final Direction direction = advice.getClass().getAnnotation(Direction.class);

        // 如果没标记则跑掉
        if (null == direction) {
            return;
        }

        for (Direction.Type type : direction.types()) {

            // 如果被标记为consumer，则放入consumer组
            if (type == Direction.Type.CONSUMER) {
                consumerAdvices.add(advice);
                continue;
            }

            if (type == Direction.Type.PROVIDER) {
                providerAdvices.add(advice);
                continue;
            }

        }//for

    }

    /**
     * 默认通知注册
     */
    static {

        // 注册runtime上下文通知点
        register(new RuntimeAdvice());

        // 注册监控通知点
        register(new ConsumerMonitorAdvice());
        register(new ProviderMonitorAdvice());

        // 注册性能通知点
        register(new ProfilerAdvice());

    }

    /**
     * 选择一个通知
     *
     * @param type
     * @return
     */
    private final static Set<Advice> switchAdvices(Direction.Type type) {
        if (type == Direction.Type.CONSUMER) {
            return consumerAdvices;
        }

        if (type == Direction.Type.PROVIDER) {
            return providerAdvices;
        }

        // 这不会发生
        return Sets.newLinkedHashSet();
    }

    /**
     * 前置通知
     *
     * @param type
     * @param runtime
     * @throws Throwable
     */
    public static void doBefores(Direction.Type type, Runtimes.Runtime runtime) throws Throwable {
        for (Advice advice : switchAdvices(type)) {
            advice.doBefore(runtime);
        }
    }

    /**
     * 后置通知
     *
     * @param type
     * @param runtime
     */
    public static void doAfter(Direction.Type type, Runtimes.Runtime runtime, Object returnObj, long cost) {
        for (Advice advice : switchAdvices(type)) {
            try {
                advice.doAfter(runtime, returnObj, cost);
            } catch (Throwable t) {
                //
            }
        }
    }

    /**
     * 异常通知
     *
     * @param type
     * @param runtime
     * @param throwable
     */
    public static void doThrow(Direction.Type type, Runtimes.Runtime runtime, Throwable throwable) {
        for (Advice advice : switchAdvices(type)) {
            try {
                advice.doThrow(runtime, throwable);
            } catch (Throwable t) {
                //
            }
        }
    }

    /**
     * 结束通知
     *
     * @param type
     * @param runtime
     */
    public static void doFinally(Direction.Type type, Runtimes.Runtime runtime) {
        for (Advice advice : switchAdvices(type)) {
            try {
                advice.doFinally(runtime);
            } catch (Throwable t) {
                //
            }
        }
    }

}
