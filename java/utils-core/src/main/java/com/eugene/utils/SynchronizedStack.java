package com.eugene.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于 synchronized 的线程安全栈（支持泛型）。
 * 使用数组存储元素，支持动态扩容。
 * 提供 push、pop、peek、isEmpty、size、clear 等基本操作。
 *
 * @param <T> 栈中元素的类型
 */
public class SynchronizedStack<T> {
    private Object[] data;      // 存储栈元素的数组
    private int top;            // 栈顶指针，-1 表示空栈
    private int capacity;       // 当前容量

    /**
     * 构造函数，初始化指定容量的栈
     *
     * @param capacity 初始容量
     */
    public SynchronizedStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    /**
     * 入栈操作，自动扩容
     *
     * @param value 入栈元素
     */
    public synchronized void push(T value) {
        if (top == capacity - 1) {
            grow(); // 栈满则扩容
        }
        data[++top] = value;
    }

    /**
     * 出栈操作，移除并返回栈顶元素
     *
     * @return 栈顶元素
     */
    @SuppressWarnings("unchecked")
    public synchronized T pop() {
        if (isEmpty()) {
            throw new RuntimeException("空栈");
        }
        return (T) data[top--];
    }

    /**
     * 查看栈顶元素但不移除
     *
     * @return 栈顶元素
     */
    @SuppressWarnings("unchecked")
    public synchronized T peek() {
        if (isEmpty()) {
            throw new RuntimeException("空栈");
        }
        return (T) data[top];
    }

    /**
     * 判断是否为空栈
     *
     * @return true 表示空
     */
    public synchronized boolean isEmpty() {
        return top == -1;
    }

    /**
     * 获取当前栈内元素数量
     *
     * @return 元素个数
     */
    public synchronized int size() {
        return top + 1;
    }

    /**
     * 清空栈中所有元素（不释放数组）
     */
    public synchronized void clear() {
        for (int i = 0; i <= top; i++) {
            data[i] = null; // 帮助 GC,清空栈,赋值为null
        }
        top = -1;
    }

    /**
     * 动态扩容，将数组大小翻倍
     */
    private void grow() {
        int newCapacity = capacity * 2;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, capacity);
        data = newData;
        capacity = newCapacity;
    }
}
