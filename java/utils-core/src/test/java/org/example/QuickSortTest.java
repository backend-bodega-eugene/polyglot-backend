
package org.example;

import com.eugene.utils.QuickSort;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class QuickSortTest {

    @Test
    public void testIntegerAscending() {
        Integer[] arr = {5, 2, 9, 1, 3};
        QuickSort.quickSort(arr);
        assertArrayEquals(new Integer[]{1, 2, 3, 5, 9}, arr);
    }

    @Test
    public void testIntegerDescending() {
        Integer[] arr = {5, 2, 9, 1, 3};
        QuickSort.quickSort(arr, false);
        assertArrayEquals(new Integer[]{9, 5, 3, 2, 1}, arr);
    }

    @Test
    public void testStringAscending() {
        String[] arr = {"banana", "apple", "orange", "pear"};
        QuickSort.quickSort(arr);
        assertArrayEquals(new String[]{"apple", "banana", "orange", "pear"}, arr);
    }

    @Test
    public void testStringDescending() {
        String[] arr = {"banana", "apple", "orange", "pear"};
        QuickSort.quickSort(arr, false);
        assertArrayEquals(new String[]{"pear", "orange", "banana", "apple"}, arr);
    }
}
