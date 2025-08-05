package org.example;

import com.eugene.utils.SynchronizedStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SynchronizedStackTest {

    @Test
    void testPushPopClear() {
        SynchronizedStack<String> stack = new SynchronizedStack<>(2);
        stack.push("A");
        stack.push("B");
        assertEquals("B", stack.pop());
        stack.clear();
        assertTrue(stack.isEmpty());
    }

    @Test
    void testGrowBehavior() {
        SynchronizedStack<Integer> stack = new SynchronizedStack<>(1);
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.size());
        assertEquals(2, stack.pop());
    }

    @Test
    void testEmptyStackPopThrows() {
        SynchronizedStack<String> stack = new SynchronizedStack<>(1);
        assertThrows(RuntimeException.class, stack::pop);
    }
}
