package xyz.scootaloo.test.thread;

import org.junit.jupiter.api.Test;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 10:24
 */
public class ThreadTest {

    @Test
    public void testThread() {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }, "1").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }, "1").start();
    }

}
