package com.eugene.utils;

import java.util.Objects;

/**
 * 简易版哈希表实现：EugeneHashMap
 * 使用拉链法处理冲突，支持基本操作：put、get、remove、containsKey、size
 * 特性：
 * - 初始容量为16，负载因子为0.9，自动扩容
 * - 使用链表解决哈希冲突
 * - 支持null键（下标0）
 */
public class EugeneHashMap<K, V> {

    /**
     * 哈希桶中的链表节点
     */
    private static class Node<K, V> {
        final K key;       // 键
        V value;           // 值
        Node<K, V> next;   // 指向下一个节点（用于拉链法）

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private float loadFactor = 0.9f; // 负载因子（超过此比例触发扩容）
    private int threshold = (int) (DEFAULT_CAPACITY * loadFactor); // 扩容阈值
    private static final int DEFAULT_CAPACITY = 16; // 初始桶数量
    private Node<K, V>[] table; // 哈希桶数组
    private int size; // 当前键值对数量

    public EugeneHashMap() {
        table = new Node[DEFAULT_CAPACITY];
    }

    /**
     * 添加或更新键值对
     * @param key 键
     * @param value 值
     */
    public void put(K key, V value) {
        int index = getIndex(key);
        Node<K, V> head = table[index];

        // 遍历链表，存在则更新值
        for (Node<K, V> node = head; node != null; node = node.next) {
            if (Objects.equals(node.key, key)) {
                node.value = value;
                return;
            }
        }

        // 不存在，头插法插入新节点
        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = head;
        table[index] = newNode;
        size++;

        // 判断是否需要扩容
        if (size >= threshold) {
            resize();
        }
    }

    /**
     * 根据key获取value
     * @param key 键
     * @return 对应的值，若不存在返回null
     */
    public V get(K key) {
        int index = getIndex(key);
        V myvalue = null;
        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (Objects.equals(node.key, key)) {
                myvalue = node.value;
                break;
            }
        }
        return myvalue;
    }

    /**
     * 判断是否包含某个key
     * @param key 键
     * @return 是否存在该键
     */
    public boolean containsKey(K key) {
        return get(key) != null;
    }

    /**
     * 移除某个key对应的键值对
     * @param key 要移除的键
     * @return 被移除的值，若不存在返回null
     */
    public V remove(K key) {
        int index = getIndex(key);
        Node<K, V> prev = null;
        Node<K, V> node = table[index];

        while (node != null) {
            if (Objects.equals(node.key, key)) {
                if (prev == null) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }
            prev = node;
            node = node.next;
        }

        return null;
    }

    /**
     * 获取当前键值对数量
     */
    public int size() {
        return size;
    }

    /**
     * 打印哈希表中所有桶及其链表结构（用于调试和教学）
     */
    public void printTableStructure() {
        System.out.println("哈希表结构（容量：" + table.length + "，当前大小：" + size + "）");
        for (int i = 0; i < table.length; i++) {
            System.out.print("桶 [" + i + "]: ");
            Node<K, V> node = table[i];
            if (node == null) {
                System.out.println("空");
                continue;
            }
            while (node != null) {
                System.out.print("[" + node.key + "=>" + node.value + "] -> ");
                node = node.next;
            }
            System.out.println("null");
        }
    }

    /**
     * 根据key计算哈希表索引
     * @param key 键
     * @return 哈希桶索引位置
     */
    private int getIndex(K key) {
        return (key == null ? 0 : key.hashCode() & 0x7fffffff) % table.length;
    }

    /**
     * 扩容并重新散列（rehash）所有键值对
     * 将容量扩展为原来的2倍
     */
    private void resize() {
        int newCapacity = table.length * 2;
        Node<K, V>[] newTable = new Node[newCapacity];

        for (int i = 0; i < table.length; i++) {
            Node<K, V> node = table[i];
            while (node != null) {
                Node<K, V> next = node.next; // 暂存旧链表的下一个节点

                // 重新计算新桶位置
                int newIndex = (node.key == null ? 0 : node.key.hashCode() & 0x7fffffff) % newCapacity;

                // 头插法放入新表
                node.next = newTable[newIndex];
                newTable[newIndex] = node;

                node = next; // 继续处理原链表
            }
        }

        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    /**
     * 主方法：测试基本功能
     */
    public static void main(String[] args) {
        EugeneHashMap<String, Integer> map = new EugeneHashMap<>();

        System.out.println("==== 插入测试 ====");
        map.put("apple", 10);
        map.put("banana", 20);
        map.put("cherry", 30);
        map.put("date", 40);
        map.printTableStructure();

        System.out.println("\n==== 获取测试 ====");
        System.out.println("apple => " + map.get("apple"));
        System.out.println("banana => " + map.get("banana"));
        System.out.println("不存在的key => " + map.get("fig"));

        System.out.println("\n==== 更新测试 ====");
        map.put("apple", 100);
        System.out.println("apple => " + map.get("apple"));

        System.out.println("\n==== 删除测试 ====");
        map.remove("banana");
        map.printTableStructure();

        System.out.println("\n==== 扩容测试 ====");
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, i);
        }
        map.printTableStructure();
        System.out.println("当前大小: " + map.size());
    }
}
