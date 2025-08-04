package com.eugene.utils;

/**
 * 工具类：数组相关操作
 */
public class ArrayUtils {


    /**
     * 反转一个泛型的数组.
     * @param array 需要反转的数组
     * @param <T>   数组的类型
     */
    public static <T> void reverse(T[] array) {
        if (array == null || array.length <= 1) return;

        int left = 0, right = array.length - 1;
        while (left < right) {
            T temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }
    /**
     * 原地反转一个 int 数组
     * @param arr 需要反转的数组
     */
    public static void reverse(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(double[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            double temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(long[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            long temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(short[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            short temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(char[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            char temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(byte[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            byte temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(float[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            float temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
    public static void reverse(boolean[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            boolean temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
}
