package com.eugene.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 线程安全的栈实现。
 * 使用 ReentrantLock 确保并发访问时的互斥性。
 * 支持动态扩容。
 *
 * @param <T> 元素类型
 */
public class SafeStack<T> {
    private Object[] data;         // 存储元素的数组
    private int top;               // 栈顶指针
    private int capacity;          // 当前容量
    private final ReentrantLock lock = new ReentrantLock(); // 可重入锁，确保线程安全

    /**
     * 构造方法，指定初始容量
     * @param capacity 初始容量
     */
    public SafeStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    /**
     * 入栈操作，必要时扩容
     * @param value 要压入的元素
     */
    public void push(T value) {
        lock.lock();
        try {
            if (top == capacity - 1) {
                grow(); // 扩容
            }
            data[++top] = value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 出栈操作
     * @return 栈顶元素
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        lock.lock();
        try {
            if (isEmpty()) throw new RuntimeException("空栈");
            return (T) data[top--];
        } finally {
            lock.unlock();
        }
    }

    /**
     * 查看栈顶元素但不移除
     * @return 栈顶元素
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        lock.lock();
        try {
            if (isEmpty()) throw new RuntimeException("空栈");
            return (T) data[top];
        } finally {
            lock.unlock();
        }
    }

    /**
     * 判断栈是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        lock.lock();
        try {
            return top == -1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取栈中元素数量
     * @return 元素数量
     */
    public int size() {
        lock.lock();
        try {
            return top + 1;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 扩容方法：容量翻倍
     */
    private void grow() {
        int newCapacity = capacity * 2;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, capacity);
        data = newData;
        capacity = newCapacity;
    }
}