package com.miniw.fescommon.utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 当前时间戳工具类
 *
 * @author luoquan
 * @date 2021/09/14
 */
public class CurrentTimeMillisUtil {

    private volatile long now;

    private static class SingletonHolder {

        private static final CurrentTimeMillisUtil INSTANCE = new CurrentTimeMillisUtil();
    }

    public static CurrentTimeMillisUtil getInstance() {

        return SingletonHolder.INSTANCE;
    }

    private CurrentTimeMillisUtil() {

        this.now = System.currentTimeMillis();
        scheduleTick();
    }

    private void scheduleTick() {

        new ScheduledThreadPoolExecutor(1, runnable -> {

            Thread thread = new Thread(runnable, "current-time-millis");
            thread.setDaemon(true);
            return thread;
        }).scheduleAtFixedRate(() -> now = System.currentTimeMillis(), 1, 1, TimeUnit.MILLISECONDS);
    }

    public long now() {
        return now;
    }

}
