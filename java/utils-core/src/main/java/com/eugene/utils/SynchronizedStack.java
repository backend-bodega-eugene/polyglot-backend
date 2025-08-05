package com.eugene.utils;

import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedStack<T> {
    private Object[] data;
    private int top;
    private int capacity;

    public SynchronizedStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    public synchronized void push(T value) {
        if (top == capacity - 1) {
            grow();
        }
        data[++top] = value;
    }

    @SuppressWarnings("unchecked")
    public synchronized T pop() {
        if (isEmpty()) {
            throw new RuntimeException("空栈");
        }
        return (T) data[top--];
    }

    @SuppressWarnings("unchecked")
    public synchronized T peek() {
        if (isEmpty()) {
            throw new RuntimeException("空栈");
        }
        return (T) data[top];
    }

    public synchronized boolean isEmpty() {
        return top == -1;
    }

    public synchronized int size() {
        return top + 1;
    }
    public synchronized void clear() {
        for (int i = 0; i <= top; i++) {
            data[i] = null; // 帮助 GC,清空栈,赋值为null
        }
        top = -1;
    }

    private void grow() {
        int newCapacity = capacity * 2;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, capacity);
        data = newData;
        capacity = newCapacity;
    }
}