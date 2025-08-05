package com.eugene.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 简易版线程池实现
 */
public class EugeneThreadPool {

    private final BlockingQueue<Runnable> taskQueue;
    private final Worker[] workers;

    public EugeneThreadPool(int poolSize) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.workers = new Worker[poolSize];

        for (int i = 0; i < poolSize; i++) {
            workers[i] = new Worker("Worker-" + i);
            workers[i].start();
        }
    }

    /**
     * 提交任务
     */
    public void execute(Runnable task) {
        if (task == null) throw new NullPointerException("task is null");
        taskQueue.offer(task); // 队列满了会阻塞（这里可以拓展成拒绝策略）
    }

    /**
     * 工作线程
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
                    task.run();
                } catch (InterruptedException e) {
                    break; // 线程池销毁时退出
                } catch (Exception e) {
                    e.printStackTrace(); // 任务异常不影响主循环
                }
            }
        }
    }

    /**
     * 停止所有线程（暴力方式）
     */
    public void shutdownNow() {
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }
}
