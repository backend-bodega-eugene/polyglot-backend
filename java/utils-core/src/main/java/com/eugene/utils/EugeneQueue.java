package com.eugene.utils;

/**
 * 自定义循环队列（数组实现）
 * @param <T> 队列中的数据类型
 */
public class EugeneQueue<T> {
    private Object[] data;
    private int front;    // 队头索引
    private int rear;     // 队尾索引
    private int size;
    private int capacity;

    public EugeneQueue(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.front = 0;
        this.rear = 0;
        this.size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == capacity;
    }

    public int size() {
        return size;
    }

    public void enqueue(T value) {
        if (isFull()) {
            throw new RuntimeException("队列满啦！");
        }
        data[rear] = value;
        rear = (rear + 1) % capacity;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("队列空啦！");
        }
        T value = (T) data[front];
        data[front] = null; // 帮助 GC
        front = (front + 1) % capacity;
        size--;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("队列空啦！");
        }
        return (T) data[front];
    }

    public void clear() {
        for (int i = 0; i < capacity; i++) {
            data[i] = null;
        }
        front = 0;
        rear = 0;
        size = 0;
    }
}
