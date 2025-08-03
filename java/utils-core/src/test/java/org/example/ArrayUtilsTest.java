package org.example;

import com.eugene.utils.ArrayUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayUtilsTest {

    @Test
    public void testReverseIntArray() {
        int[] arr = {1, 2, 3, 4, 5};
        ArrayUtils.reverse(arr);
        assertArrayEquals(new int[]{5, 4, 3, 2, 1}, arr);
    }

    @Test
    public void testReverseStringArray() {
        String[] arr = {"老胡", "最", "帅"};
        ArrayUtils.reverse(arr);
        assertArrayEquals(new String[]{"帅", "最", "老胡"}, arr);
    }

    @Test
    public void testReverseEmptyArray() {
        Integer[] arr = {};
        ArrayUtils.reverse(arr);
        assertArrayEquals(new Integer[]{}, arr);
    }

    @Test
    public void testReverseBoolean() {
        boolean[] arr = {true, false, false};
        ArrayUtils.reverse(arr);
        assertArrayEquals(new boolean[]{false, false, true}, arr);
    }
}
