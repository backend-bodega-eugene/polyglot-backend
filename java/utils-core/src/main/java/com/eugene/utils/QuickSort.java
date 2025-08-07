package com.eugene.utils;

/**
 * 快速排序：适用于 int 类型数组，原地排序
 */
public class QuickSort {

    /**
     * 默认升序排序。
     *
     * @param arr 待排序数组
     * @param <T> 类型参数，要求实现 Comparable 接口
     */
    public static <T extends Comparable<T>> void quickSort(T[] arr) {
        quickSort(arr, true);
    }

    /**
     * 可指定升序/降序的快速排序方法。
     *
     * @param arr       待排序数组
     * @param ascending true 表示升序，false 表示降序
     * @param <T>       类型参数，要求实现 Comparable 接口
     */
    public static <T extends Comparable<T>> void quickSort(T[] arr, boolean ascending) {
        quickSortRecursive(arr, 0, arr.length - 1, ascending);
    }

    /**
     * 快速排序递归逻辑
     *
     * @param arr       待排序数组
     * @param left      左边界
     * @param right     右边界
     * @param ascending 是否升序排序
     * @param <T>       类型参数
     */
    private static <T extends Comparable<T>> void quickSortRecursive(T[] arr, int left, int right, boolean ascending) {
        if (left >= right) return;

        int pivotIndex = partition(arr, left, right, ascending);
        quickSortRecursive(arr, left, pivotIndex - 1, ascending);
        quickSortRecursive(arr, pivotIndex + 1, right, ascending);
    }

    /**
     * 分区操作，将 pivot 左右划分并返回新下标
     *
     * @param arr       待处理数组
     * @param left      左边界
     * @param right     右边界
     * @param ascending 是否升序
     * @param <T>       类型参数
     * @return pivot 的新位置
     */
    private static <T extends Comparable<T>> int partition(T[] arr, int left, int right, boolean ascending) {
        T pivot = arr[right];
        int i = left - 1;

        for (int j = left; j < right; j++) {
            int cmp = arr[j].compareTo(pivot);
            if ((ascending && cmp <= 0) || (!ascending && cmp >= 0)) {
                i++;
                swap(arr, i, j);
            }
        }

        swap(arr, i + 1, right);
        return i + 1;
    }

    /**
     * 数组中两个元素交换
     *
     * @param arr 数组
     * @param i   索引1
     * @param j   索引2
     * @param <T> 类型参数
     */
    private static <T> void swap(T[] arr, int i, int j) {
        if (i != j) {
            T temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * 打印数组内容。
     *
     * @param arr 数组
     * @param <T> 类型参数
     */
    public static <T> void printArray(T[] arr) {
        for (T t : arr) {
            System.out.print(t + " ");
        }
        System.out.println();
    }
}