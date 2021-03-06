package xyz.scootaloo.test.thread;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.client.ResourcesHandler;
import xyz.scootaloo.console.app.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/5 22:14
 */
public class ExtraOptionalInThread {

    @Test
    public void testVariable() throws InterruptedException {
        Interpreter interpreter = ApplicationRunner.getInterpreter();
        new Thread(() -> {
            ResourcesHandler handler = interpreter.setUser("A");
            System.out.println("------------线程A---------------");
            interpreter.interpret("set num1 12");
            interpreter.interpret("set num2 13");
            String number = interpreter.interpret("get num1").get();
            System.out.println("线程A中得到的结果是: " + number);
            System.out.println("--------------------------------");
//            handler.shutdown(); // 销毁用户A的资源；假如取消这行注释，则存储在用户A资源中已经存储的变量池被清空，第33行代码因为占位符不能替换而抛出异常
        }, "A").start();
        Thread.sleep(100);
        new Thread(() -> {
            // 让用户A重新进入
            ResourcesHandler handler = interpreter.setUser("A");
            System.out.println("------------线程B---------------");
            interpreter.interpret("B ${num1} ${num2}");
            System.out.println("--------------------------------");
        }, "B").start();
    }

    @Test
    public void testBackstageTasks() throws InterruptedException {
        Interpreter interpreter = ApplicationRunner.getInterpreter();
        new Thread(() -> {
            ResourcesHandler handler = interpreter.setUser("A");
            System.out.println("------------线程A---------------");
            // 提交两个任务
            interpreter.interpret("A-Dta");
            interpreter.interpret("A-Dtb");
            interpreter.interpret("tasks"); // 查看执行结果
            System.out.println("--------------------------------");
            handler.shutdown(); // 销毁用户A的资源, 假如注释这一行，则线程A中提交的任务对于线程B可见
        }, "A").start();
        Thread.sleep(100);
        new Thread(() -> {
            // 让用户A重新进入
            ResourcesHandler handler = interpreter.setUser("A");
            System.out.println("------------线程B---------------");
            interpreter.interpret("A-Dtc"); // 提交一个任务
            interpreter.interpret("tasks"); // 查看执行结果
            System.out.println("--------------------------------");
        }, "B").start();
    }

    @Cmd
    public void A() {
    }

    @Cmd
    public void B(int a, int b) {
        System.out.println("[" + Thread.currentThread().getName() + "] a + b = " + (a + b));
    }

}
