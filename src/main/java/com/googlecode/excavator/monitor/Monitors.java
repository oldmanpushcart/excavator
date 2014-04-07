package com.googlecode.excavator.monitor;

import static com.googlecode.excavator.PropertyConfiger.isEnableMonitor;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.googlecode.excavator.constant.Log4jConstant;

/**
 * 监控工具类
 *
 * @author vlinux
 *
 */
public final class Monitors {

    private static final Logger logger = Logger.getLogger(Log4jConstant.MONITOR);
    private static Map<String/*getKey()*/, AtomicReference<Monitor>> monitors = Maps.newConcurrentMap();

    private static final long MONITOR_SLEEP = 60000 * 2;//休眠2分钟

    static {

        // 只有启用了监控才打开监控线程
        if (isEnableMonitor()) {
            // 开启守护线程
            startMonitor();
        }

    }

    /**
     * 启动monitor守护线程
     */
    private static void startMonitor() {
        // 监控守护线程
        final Thread monitor = new Thread("monitor-daemon") {

            @Override
            public void run() {
                while (true) {
                    final ReentrantLock lock = new ReentrantLock();
                    final Condition condition = lock.newCondition();
                    lock.lock();
                    try {
                        condition.await(MONITOR_SLEEP, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        //
                    } finally {
                        lock.unlock();
                    }

                    flushMonitorLog();

                }// while

            }

        };
        monitor.setDaemon(true);
        monitor.start();

		// 关闭之前务必先将监控刷到日志中
        // 这段代码似乎没起作用，先放着
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                flushMonitorLog();
            }

        });
    }

    /**
     * 将monitor信息刷到日志
     */
    private synchronized static void flushMonitorLog() {
//		final DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
//		final String now = df.format(new Date());
        for (AtomicReference<Monitor> ref : monitors.values()) {
            final Monitor m = resetMonitorRef(ref);
            final StringBuilder monitorSB = new StringBuilder();
//			monitorSB.append(now).append("\t");
            monitorSB.append(m.getType()).append("\t");
            monitorSB.append(m.getGroup()).append("\t");
            monitorSB.append(m.getVersion()).append("\t");
            monitorSB.append(m.getSign()).append("\t");
            monitorSB.append(m.getFrom()).append("\t");
            monitorSB.append(m.getTo()).append("\t");
            monitorSB.append(m.getTimes()).append("\t");
            monitorSB.append(m.getCost()).append("\t");
            logger.info(monitorSB.toString());
        }

    }

    /**
     * 记录日志
     *
     * @param type
     * @param group
     * @param version
     * @param sign
     * @param key
     * @param from
     * @param to
     * @param cost
     */
    public static void monitor(
            final Monitor.Type type,
            final String group,
            final String version,
            final String sign,
            final String from,
            final String to,
            final long cost) {

        final String key = getKey(type, group, version, sign, from, to);

        while (true) {
            AtomicReference<Monitor> ref = monitors.get(key);
            if (null == ref) {
                ref = new AtomicReference<Monitor>();
                ref.set(statistics(new Monitor(type, group, version, sign, from, to), cost));
                monitors.put(key, ref);
            }
            final Monitor oMonitor = ref.get();
            final Monitor nMonitor = oMonitor.clone();
            if (ref.compareAndSet(oMonitor, statistics(nMonitor, cost))) {
                break;
            }

        }

    }

    /**
     * 统计监控
     *
     * @param monitor
     * @param isFailed
     * @param cost
     * @return
     */
    private static Monitor statistics(Monitor monitor, long cost) {
        monitor.setTimes(monitor.getTimes() + 1);
        monitor.setCost(monitor.getCost() + cost);
        return monitor;
    }

    /**
     * 重设置ref的值
     *
     * @param ref
     */
    private static Monitor resetMonitorRef(AtomicReference<Monitor> ref) {
        while (true) {
            final Monitor oMonitor = ref.get();
            if (ref.compareAndSet(oMonitor, new Monitor(oMonitor))) {
                return oMonitor;
            }
        }
    }

    /**
     * 获取监控key
     *
     * @param type
     * @param group
     * @param version
     * @param sign
     * @param from
     * @param to
     * @return
     */
    private static String getKey(Monitor.Type type, String group, String version, String sign, String from, String to) {
        return type + group + version + sign + from + to;
    }

}
