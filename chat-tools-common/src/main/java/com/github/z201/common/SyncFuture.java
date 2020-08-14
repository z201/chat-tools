package com.github.z201.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author z201.coding@gmail.com
 **/
public class SyncFuture<MessageHolder> implements Future<MessageHolder> {

    private static volatile SyncFuture single = null;

    private SyncFuture() {

    }

    public static SyncFuture getInstance() {
        if (null == single) {
            synchronized (SyncFuture.class) {
                if (null == single) {
                    single = new SyncFuture();
                }
            }
        }
        return single;
    }

    /**
     * 因为请求和响应是一一对应的，因此初始化CountDownLatch值为1。
     */
    private CountDownLatch latch = new CountDownLatch(1);
    /**
     * 需要响应线程设置的响应结果
     */
    private MessageHolder response;
    // Futrue的请求时间，用于计算Future是否超时
    private long beginTime;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        if (response != null) {
            return true;
        }
        return false;
    }

    // 获取响应结果，直到有结果才返回。
    @Override
    public MessageHolder get() throws InterruptedException {
        latch.await();
        return this.response;
    }

    // 获取响应结果，直到有结果或者超过指定时间就返回。
    @Override
    public MessageHolder get(long timeout, TimeUnit unit) throws InterruptedException {
        if (latch.await(timeout, unit)) {
            return this.response;
        }
        return null;
    }

    // 用于设置响应结果，并且做countDown操作，通知请求线程
    public void setResponse(MessageHolder response) {
        this.response = response;
        beginTime = System.currentTimeMillis();
        latch.countDown();
    }

    public long getBeginTime() {
        return beginTime;
    }
}
