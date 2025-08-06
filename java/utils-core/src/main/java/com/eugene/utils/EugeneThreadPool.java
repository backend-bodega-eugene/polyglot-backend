package com.eugene.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 简易版线程池实现：EugeneThreadPool
 * 支持任务提交、执行、暴力关闭
 * 内部维护固定数量的工作线程，使用阻塞队列传递任务
 */
public class EugeneThreadPool {

    private final BlockingQueue<Runnable> taskQueue;  // 任务队列
    private final Worker[] workers;                   // 工作线程数组

    /**
     * 构造函数：创建线程池并启动所有工作线程
     * @param poolSize 线程池大小
     */
    public EugeneThreadPool(int poolSize) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new Worker[poolSize];

        for (int i = 0; i < poolSize; i++) {
            workers[i] = new Worker("Worker-" + i);
            workers[i].start();
        }
    }

    /**
     * 提交任务到线程池
     * @param task 待执行任务
     */
    public void execute(Runnable task) {
        if (task == null) throw new NullPointerException("task is null");
        taskQueue.offer(task); // 任务放入队列（可扩展为阻塞或拒绝策略）
    }

    /**
     * 内部类：工作线程负责从队列中取出任务并执行
     */
    private class Worker extends Thread {
        public Worker(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = taskQueue.take(); // 阻塞等待任务
                    task.run();                       // 执行任务
                } catch (InterruptedException e) {
                    break; // 被中断时退出（用于 shutdownNow）
                } catch (Exception e) {
                    e.printStackTrace(); // 防止单个任务异常影响主循环
                }
            }
        }
    }

    /**
     * 立即关闭线程池：中断所有工作线程
     */
    public void shutdownNow() {
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }
}
