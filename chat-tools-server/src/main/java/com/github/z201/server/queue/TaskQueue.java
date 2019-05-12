package com.github.z201.server.queue;


import com.github.z201.common.protocol.MessageHolder;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 接收阻塞队列，缓存刚入站的任务.
 *
 * Transport Module ---> InboundQueue ---> Service Module.
 *
 * @author z201.coding@gmail.com.
 */
public class TaskQueue {
    private volatile static BlockingQueue<MessageHolder> queue;

    public static BlockingQueue<MessageHolder> getQueue() {
        if (queue == null) {
            synchronized (TaskQueue.class) {
                if (queue == null) {
                    queue = new LinkedBlockingDeque<>(1024);
                }
            }
        }
        return queue;
    }
}
