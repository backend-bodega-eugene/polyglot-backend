package com.eugene.utils;

import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 双指针去重 - 泛型版（适用于有序数组）
 */
public class GenericDeduplicator {

    /**
     * 泛型数组去重（原地 + 有序数组）
     *
     * @param array 有序数组
     * @param clazz 类型（用于创建新数组）
     * @return 去重后的新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] deduplicate(T[] array, Class<T> clazz) {
        if (array == null || array.length == 0) {
            return (T[]) Array.newInstance(clazz, 0);
        }
        int slow = 0;
        for (int fast = 1; fast < array.length; fast++) {
            if (!Objects.equals(array[fast], array[slow])) {
                array[++slow] = array[fast];
            }
        }
        T[] result = (T[]) Array.newInstance(clazz, slow + 1);
        System.arraycopy(array, 0, result, 0, slow + 1);
        return result;
    }
    /**
     * 泛型数组去重（使用 LinkedHashSet 保证顺序）
     *
     * @param array 输入数组
     * @param clazz 数组元素类型（用于构造新数组）
     * @return 去重后的新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] deduplicateWithSet(T[] array, Class<T> clazz) {
        if (array == null || array.length == 0) {
            return (T[]) Array.newInstance(clazz, 0);
        }
        Set<T> set = new LinkedHashSet<>();
        for (T item : array) {
            set.add(item);
        }
        T[] result = (T[]) Array.newInstance(clazz, set.size());
        int i = 0;
        for (T item : set) {
            result[i++] = item;
        }
        return result;
    }
}
