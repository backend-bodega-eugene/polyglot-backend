package com.eugene.utils;

import java.util.concurrent.locks.ReentrantLock;

public class SafeStack<T> {
    private Object[] data;
    private int top;
    private int capacity;
    private final ReentrantLock lock = new ReentrantLock();

    public SafeStack(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.top = -1;
    }

    public void push(T value) {
        lock.lock();
        try {
            if (top == capacity - 1) {
                grow();
            }
            data[++top] = value;
        } finally {
            lock.unlock();
        }
    }

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

    public boolean isEmpty() {
        lock.lock();
        try {
            return top == -1;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return top + 1;
        } finally {
            lock.unlock();
        }
    }

    private void grow() {
        int newCapacity = capacity * 2;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, capacity);
        data = newData;
        capacity = newCapacity;
    }
}
