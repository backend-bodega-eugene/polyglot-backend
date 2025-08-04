package com.eugene.utils;

/**
 * 通用冒泡排序工具类，支持任意实现了 Comparable 接口的包装类型。
 * 提供升序与降序排序的功能。
 *
 * 支持类型示例：
 * Integer, Double, Long, Float, Character, String 等包装类型。
 */
public class GenericBubbleSort {


    /**
     * 对数组进行冒泡排序，默认升序。
     *
     * @param arr 待排序的数组
     * @param <T> 类型参数，要求实现 Comparable 接口
     */
    public static <T extends Comparable<T>> void bubbleSort(T[] arr) {
        bubbleSort(arr, true);
    }

    /**
     * 对数组进行冒泡排序，可指定升序或降序。
     *
     * @param arr       待排序的数组
     * @param ascending true 表示升序，false 表示降序
     * @param <T>       类型参数，要求实现 Comparable 接口
     */
    public static <T extends Comparable<T>> void bubbleSort(T[] arr, boolean ascending) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                // 比较逻辑根据 ascending 决定
                int cmp = arr[j].compareTo(arr[j + 1]);
                if ((ascending && cmp > 0) || (!ascending && cmp < 0)) {
                    T temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
    }

    /**
     * 打印数组内容，格式为用空格分隔的元素。
     *
     * @param arr 待打印的数组
     * @param <T> 类型参数
     */
    public static <T> void printArray(T[] arr) {
        for (T item : arr) {
            System.out.print(item + " ");
        }
        System.out.println();
    }
}
