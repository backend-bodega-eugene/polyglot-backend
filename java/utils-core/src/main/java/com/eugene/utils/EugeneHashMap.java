package com.eugene.utils;

import java.util.Objects;

/**
 * 简易版哈希表实现：EugeneHashMap
 * 使用拉链法处理冲突，支持基本操作
 */
public class EugeneHashMap<K, V> {

    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    private float loadFactor = 0.9f;
    private int threshold = (int) (DEFAULT_CAPACITY * loadFactor);
    private static final int DEFAULT_CAPACITY = 16;
    private Node<K, V>[] table;
    private int size;

    public EugeneHashMap() {
        table = new Node[DEFAULT_CAPACITY];
    }

    public void put(K key, V value) {
        int index = getIndex(key);
        Node<K, V> head = table[index];

        for (Node<K, V> node = head; node != null; node = node.next) {
            if (Objects.equals(node.key, key)) {
                node.value = value;
                return;
            }
        }

        Node<K, V> newNode = new Node<>(key, value);
        newNode.next = head;
        table[index] = newNode;
        size++;
        if (size >= threshold) {
            resize();
        }
    }

    public V get(K key) {
        int index = getIndex(key);
        V myvalue = null;
        for (Node<K, V> node = table[index]; node != null; node = node.next) {
            if (Objects.equals(node.key, key)) {
                
                myvalue= node.value;
                break;
            }
        }
        return myvalue;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

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

        // 更新阈值
        threshold = (int) (newCapacity * loadFactor);
    }
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
