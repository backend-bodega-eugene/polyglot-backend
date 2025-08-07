package com.eugene.utils;

/**
 * 自定义循环队列（数组实现）
 * 支持泛型，固定容量，基于数组结构实现环形逻辑
 * 操作包括：入队、出队、获取队头、判空、判满、清空
 *
 * @param <T> 队列中的数据类型
 */
public class EugeneQueue<T> {
    private Object[] data;  // 存储队列元素的数组
    private int front;      // 队头索引，指向当前元素
    private int rear;       // 队尾索引，指向下一个可插入位置
    private int size;       // 当前队列中的元素数量
    private int capacity;   // 队列总容量

    /**
     * 构造方法：指定队列容量
     * @param capacity 队列最大长度
     */
    public EugeneQueue(int capacity) {
        this.capacity = capacity;
        this.data = new Object[capacity];
        this.front = 0;
        this.rear = 0;
        this.size = 0;
    }

    /**
     * 判断队列是否为空
     * @return 是否为空
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 判断队列是否已满
     * @return 是否已满
     */
    public boolean isFull() {
        return size == capacity;
    }

    /**
     * 获取当前队列中元素数量
     * @return 元素个数
     */
    public int size() {
        return size;
    }

    /**
     * 入队操作：将元素插入队尾
     * @param value 要插入的元素
     */
    public void enqueue(T value) {
        if (isFull()) {
            throw new RuntimeException("队列满啦！");
        }
        data[rear] = value;
        rear = (rear + 1) % capacity;
        size++;
    }

    /**
     * 出队操作：移除并返回队头元素
     * @return 队头元素
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("队列空啦！");
        }
        T value = (T) data[front];
        data[front] = null; // 帮助 GC 回收对象
        front = (front + 1) % capacity;
        size--;
        return value;
    }

    /**
     * 获取队头元素但不移除
     * @return 队头元素
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("队列空啦！");
        }
        return (T) data[front];
    }

    /**
     * 清空队列（不释放内存）
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            data[i] = null;
        }
        front = 0;
        rear = 0;
        size = 0;
    }
}
