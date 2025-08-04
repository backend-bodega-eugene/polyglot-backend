package org.example;

import com.eugene.utils.GenericBubbleSort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class GenericBubbleSortTest {

    @Test
    public void testIntegerAscending() {
        Integer[] arr = {4, 2, 7, 1, 3};
        GenericBubbleSort.bubbleSort(arr);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 7}, arr);
    }

    @Test
    public void testIntegerDescending() {
        Integer[] arr = {4, 2, 7, 1, 3};
        GenericBubbleSort.bubbleSort(arr, false);
        assertArrayEquals(new Integer[]{7, 4, 3, 2, 1}, arr);
    }

    @Test
    public void testStringAscending() {
        String[] arr = {"pear", "banana", "apple", "orange"};
        GenericBubbleSort.bubbleSort(arr);
        assertArrayEquals(new String[]{"apple", "banana", "orange", "pear"}, arr);
    }

    @Test
    public void testStringDescending() {
        String[] arr = {"pear", "banana", "apple", "orange"};
        GenericBubbleSort.bubbleSort(arr, false);
        assertArrayEquals(new String[]{"pear", "orange", "banana", "apple"}, arr);
    }
}
