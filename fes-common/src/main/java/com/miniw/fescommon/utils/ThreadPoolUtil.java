package com.miniw.fescommon.utils;

import java.util.concurrent.*;

/**
 * 线程池管理的工具类，封装类
 *
 * @author luoquan
 * @date 2021/07/30
 */
public class ThreadPoolUtil {
    /**
     * 通过ThreadPoolExecutor的代理类来对线程池的管理
     */
    private static ThreadPollProxy mThreadPollProxy;


    /**
     * ThreadPool DCL
     *
     * @return {@link ThreadPollProxy}
     */
    public static ThreadPollProxy getThreadPollProxy() {
        synchronized (ThreadPollProxy.class) {
            if (mThreadPollProxy == null) {
                mThreadPollProxy = new ThreadPollProxy(Runtime.getRuntime().availableProcessors() * 2,
                        Runtime.getRuntime().availableProcessors() * 4,
                        30);
            }
        }
        return mThreadPollProxy;
    }


    /**
     * 通过ThreadPoolExecutor的代理类来对线程池的管理
     *
     * @date 2021/07/30
     */
    public static class ThreadPollProxy {
        public ThreadPoolExecutor poolExecutor;
        private final int corePoolSize;
        private final int maximumPoolSize;
        private final long keepAliveTime;

        public ThreadPollProxy(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
            poolExecutor = new ThreadPoolExecutor(
                    // 核心线程数量
                    corePoolSize,
                    // 最大线程数量
                    maximumPoolSize,
                    // 当线程空闲时，保持活跃的时间
                    keepAliveTime,
                    // 时间单元 ，毫秒级
                    TimeUnit.SECONDS,
                    // 线程任务队列
                    new LinkedBlockingQueue<>(20000),
                    // 创建线程的工厂
                    Executors.defaultThreadFactory(),
                    // 拒绝策略，直接丢弃
                    new ThreadPoolExecutor.AbortPolicy());
        }

        //对外提供一个执行任务的方法
        public void execute(Runnable r) {
            if (poolExecutor == null || poolExecutor.isShutdown()) {
                poolExecutor = new ThreadPoolExecutor(
                        // 核心线程数量
                        corePoolSize,
                        // 最大线程数量
                        maximumPoolSize,
                        // 当线程空闲时，保持活跃的时间
                        keepAliveTime,
                        // 时间单元 ，毫秒级
                        TimeUnit.SECONDS,
                        // 线程任务队列
                        new LinkedBlockingQueue<>(20000),
                        // 创建线程的工厂
                        Executors.defaultThreadFactory(),
                        // 拒绝策略，直接丢弃
                        new ThreadPoolExecutor.AbortPolicy());
            }
            poolExecutor.execute(r);
        }

        // 对外提供一个执行任务的方法
        public <T> Future<T> submit(Callable r) {
            if (poolExecutor == null || poolExecutor.isShutdown()) {
                poolExecutor = new ThreadPoolExecutor(
                        // 核心线程数量
                        corePoolSize,
                        // 最大线程数量
                        maximumPoolSize,
                        // 当线程空闲时，保持活跃的时间
                        keepAliveTime,
                        // 时间单元 ，毫秒级
                        TimeUnit.SECONDS,
                        // 线程任务队列
                        new LinkedBlockingQueue<>(20000),
                        // 创建线程的工厂
                        Executors.defaultThreadFactory(),
                        // 拒绝策略，直接丢弃
                        new ThreadPoolExecutor.AbortPolicy());
            }
            return poolExecutor.submit(r);
        }
    }
}

