package org.example;

import com.eugene.utils.GenericDeduplicator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

class GenericDeduplicatorTest {

    @Test
    void testDeduplicateOrdered() {
        Integer[] input = {1, 1, 2, 2, 3, 3};
        Integer[] expected = {1, 2, 3};
        Integer[] result = GenericDeduplicator.deduplicate(input, Integer.class);
        assertArrayEquals(expected, result);
    }

    @Test
    void testDeduplicateWithSet() {
        String[] input = {"a", "b", "a", "c", "b"};
        String[] expected = {"a", "b", "c"};
        String[] result = GenericDeduplicator.deduplicateWithSet(input, String.class);
        assertArrayEquals(expected, result);
    }

    @Test
    void testDeduplicateEmptyArray() {
        Integer[] input = {};
        Integer[] result = GenericDeduplicator.deduplicate(input, Integer.class);
        assertEquals(0, result.length);
    }

    @Test
    void testDeduplicateSingleElement() {
        Integer[] input = {42};
        Integer[] result = GenericDeduplicator.deduplicate(input, Integer.class);
        assertArrayEquals(new Integer[]{42}, result);
    }

    @Test
    void testDeduplicateAllSame() {
        String[] input = {"x", "x", "x"};
        String[] result = GenericDeduplicator.deduplicate(input, String.class);
        assertArrayEquals(new String[]{"x"}, result);
    }
}
