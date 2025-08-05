package org.example;

import com.eugene.utils.EugeneThreadPool;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class EugeneThreadPoolTest {

    @Test
    void testMultipleTasksExecuted() throws InterruptedException {
        EugeneThreadPool pool = new EugeneThreadPool(3);
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            pool.execute(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        latch.await(); // 等所有任务执行完
        assertEquals(5, counter.get());

        pool.shutdownNow();
    }

    @Test
    void testExecuteNullTaskThrowsException() {
        EugeneThreadPool pool = new EugeneThreadPool(1);
        assertThrows(NullPointerException.class, () -> pool.execute(null));
        pool.shutdownNow();
    }

    @Test
    void testShutdownNowInterruptsThreads() throws InterruptedException {
        EugeneThreadPool pool = new EugeneThreadPool(1);
        Thread.sleep(100); // 给线程池一点时间启动
        pool.shutdownNow(); // 中断线程
        // 没有明确验证，但应不抛异常
    }
}
