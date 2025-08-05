package org.example;

import com.eugene.utils.SafeStack;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SafeStackTest {

    @Test
    void testPushPopPeek() {
        SafeStack<String> stack = new SafeStack<>(2);
        stack.push("A");
        stack.push("B");
        assertEquals("B", stack.peek());
        assertEquals("B", stack.pop());
        assertEquals("A", stack.pop());
    }

    @Test
    void testGrow() {
        SafeStack<Integer> stack = new SafeStack<>(1);
        stack.push(1);
        stack.push(2);
        assertEquals(2, stack.size());
        assertEquals(2, stack.pop());
    }

    @Test
    void testIsEmpty() {
        SafeStack<String> stack = new SafeStack<>(2);
        assertTrue(stack.isEmpty());
        stack.push("x");
        assertFalse(stack.isEmpty());
    }

    @Test
    void testPopFromEmptyThrows() {
        SafeStack<Integer> stack = new SafeStack<>(1);
        assertThrows(RuntimeException.class, stack::pop);
    }
}
