package com.eugene.utils;

/**
 * 工具类：数组相关操作集合
 * <p>
 * 当前仅包含各类型数组的原地反转操作
 * </p>
 * @author Eugene
 */
public class ArrayUtils {

    /**
     * 反转一个泛型数组（通用版本）
     *
     * @param array 需要反转的数组
     * @param <T>   数组元素的类型
     */
    public static <T> void reverse(T[] array) {
        if (array == null || array.length <= 1) return;

        int left = 0, right = array.length - 1;
        while (left < right) {
            // 交换左右元素
            T temp = array[left];
            array[left] = array[right];
            array[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * 原地反转一个 int 类型数组
     *
     * @param arr 需要反转的数组
     */
    public static void reverse(int[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            // 交换左右位置的元素
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }

    /**
     * 原地反转一个 double 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 long 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 short 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 char 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 byte 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 float 类型数组
     *
     * @param arr 需要反转的数组
     */
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

    /**
     * 原地反转一个 boolean 类型数组
     *
     * @param arr 需要反转的数组
     */
    public static void reverse(boolean[] arr) {
        if (arr == null || arr.length <= 1) return;

        int left = 0, right = arr.length - 1;
        while (left < right) {
            // 交换左右的布尔值
            boolean temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
}
