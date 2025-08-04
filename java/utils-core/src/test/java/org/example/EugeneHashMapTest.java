package org.example;

import com.eugene.utils.EugeneHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class EugeneHashMapTest {

    private EugeneHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new EugeneHashMap<>();
    }

    @Test
    void testPutAndGet() {
        map.put("apple", 10);
        map.put("banana", 20);
        map.put("orange", 30);

        assertEquals(10, map.get("apple"));
        assertEquals(20, map.get("banana"));
        assertEquals(30, map.get("orange"));
    }

    @Test
    void testOverwriteValue() {
        map.put("key", 1);
        map.put("key", 99);

        assertEquals(99, map.get("key"));
        assertEquals(1, map.size());
    }

    @Test
    void testContainsKey() {
        map.put("exist", 42);
        assertTrue(map.containsKey("exist"));
        assertFalse(map.containsKey("not-exist"));
    }

    @Test
    void testRemove() {
        map.put("deleteMe", 100);
        assertEquals(100, map.remove("deleteMe"));
        assertNull(map.get("deleteMe"));
        assertEquals(0, map.size());
    }

    @Test
    void testResize() {
        // 插入超过默认容量16 * 0.9 = 14个元素，触发resize
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, i);
        }

        for (int i = 0; i < 20; i++) {
            assertEquals(i, map.get("key" + i));
        }

        assertEquals(20, map.size());
    }
}
