package com.github.z201.server.service;

import com.github.z201.common.protocol.MessageHolder;
import com.github.z201.server.queue.TaskQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The implementation of Service.
 *
 * @author z201.coding@gmail.com.
 */
public class Service {
    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    public static AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * 任务队列
     */
    private BlockingQueue<MessageHolder> taskQueue;
    /**
     * 阻塞式地从taskQueue取MessageHolder
     */
    private ExecutorService takeExecutor;
    /**
     * 执行业务的线程池
     */
    private ExecutorService taskExecutor;

    public void initAndStart() {
        init();
        start();
    }

    private void init() {
        takeExecutor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        taskExecutor = new ThreadPoolExecutor(10, 10,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        taskQueue = TaskQueue.getQueue();
        logger.info("初始化服务完成");
    }

    private void start() {
        takeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while (!shutdown.get()) {
                    try {
                        MessageHolder messageHolder = taskQueue.take();
                        logger.info("TaskQueue取出任务: taskQueue=" + taskQueue.size());
                        startTask(messageHolder);
                    } catch (InterruptedException e) {
                        logger.warn("receiveQueue take", e);
                    }
                }
            }

            private void startTask(MessageHolder messageHolder) {
                taskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        logger.info("开始执行取出的任务 messageHolder=" + messageHolder);
                        Dispatcher.dispatch(messageHolder);
                    }
                });
            }
        });
        logger.info("启动服务完成");
    }
}
