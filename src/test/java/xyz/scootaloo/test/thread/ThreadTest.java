package xyz.scootaloo.test.thread;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.client.ClientCenter;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 10:24
 */
public class ThreadTest {

    public static void main(String[] args) throws InterruptedException {
        Interpreter interpreter = ApplicationRunner.getInterpreter(
                Console.factories().add(ThreadTest.class).ok());
        Thread thread1 = new Thread(() -> {
            interpreter.createUser("test1");
            interpreter.interpret("a");
            interpreter.interpret("c");
            System.out.println("-------------------" + Thread.currentThread().getName());
            interpreter.interpret("his");
        }, "A");

        Thread thread2 = new Thread(() -> {
            ClientCenter.DestroyResources callback = interpreter.createUser("test3");
            interpreter.interpret("b");
            interpreter.interpret("b");
            System.out.println("-------------------" + Thread.currentThread().getName());
            interpreter.interpret("his");
            callback.shutdown();
            ClientCenter.show();
        }, "B");

        Thread thread3 = new Thread(() -> {
            interpreter.createUser("test2");
            interpreter.interpret("c");
            interpreter.interpret("c");
            System.out.println("-------------------" + Thread.currentThread().getName());
            interpreter.interpret("his");
        }, "C");

        thread1.start();
        Thread.sleep(100);
        thread2.start();
        Thread.sleep(100);
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        ClientCenter.show();
    }

    @Test
    public void testThread() {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }, "1").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        }, "1").start();
    }

    @Cmd
    public void a() {

    }

    @Cmd
    public void b() {

    }

    @Cmd
    public void c() {

    }

}
