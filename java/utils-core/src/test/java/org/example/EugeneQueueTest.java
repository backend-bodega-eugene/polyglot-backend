package org.example;

import com.eugene.utils.EugeneQueue;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EugeneQueueTest {

    @Test
    void testEnqueueDequeue() {
        EugeneQueue<String> queue = new EugeneQueue<>(3);
        queue.enqueue("a");
        queue.enqueue("b");
        assertEquals("a", queue.dequeue());
        assertEquals("b", queue.dequeue());
    }

    @Test
    void testOverflow() {
        EugeneQueue<Integer> queue = new EugeneQueue<>(2);
        queue.enqueue(1);
        queue.enqueue(2);
        assertThrows(RuntimeException.class, () -> queue.enqueue(3));
    }

    @Test
    void testUnderflow() {
        EugeneQueue<Integer> queue = new EugeneQueue<>(2);
        assertThrows(RuntimeException.class, queue::dequeue);
    }

    @Test
    void testCircularBehavior() {
        EugeneQueue<String> queue = new EugeneQueue<>(3);
        queue.enqueue("1");
        queue.enqueue("2");
        queue.dequeue();
        queue.enqueue("3");
        queue.enqueue("4"); // should wrap around
        assertTrue(queue.isFull());
        assertEquals("2", queue.dequeue());
        assertEquals("3", queue.dequeue());
        assertEquals("4", queue.dequeue());
    }

    @Test
    void testPeek() {
        EugeneQueue<String> queue = new EugeneQueue<>(2);
        queue.enqueue("x");
        assertEquals("x", queue.peek());
        assertEquals("x", queue.peek());
    }

    @Test
    void testClear() {
        EugeneQueue<String> queue = new EugeneQueue<>(2);
        queue.enqueue("a");
        queue.enqueue("b");
        queue.clear();
        assertTrue(queue.isEmpty());
        assertThrows(RuntimeException.class, queue::peek);
    }
}
