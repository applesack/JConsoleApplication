package xyz.scootaloo.test.thread;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.client.ClientCenter;
import xyz.scootaloo.console.app.client.ResourcesHandler;
import xyz.scootaloo.console.app.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/4 9:33
 */
public class SingleThreadTest {

    public static void main(String[] args) {
        Interpreter interpreter = ApplicationRunner.getInterpreter();
        ResourcesHandler resourcesHandler = interpreter.createUser("A");
        interpreter.interpret("a");
        interpreter.interpret("a");
        resourcesHandler.shutdown();
        interpreter.createUser("A");
        interpreter.interpret("b");
        interpreter.interpret("b");
        interpreter.interpret("his");
    }

    @Test
    public void testDoubleThread() throws InterruptedException {
        Interpreter interpreter = ApplicationRunner.getInterpreter();
        Thread t1 = new Thread(() -> {
            ResourcesHandler resourcesHandler = interpreter.createUser("A");
            interpreter.interpret("a");
            interpreter.interpret("a");
//            resourcesHandler.shutdown(); // 假如取消这行注释，则输出 bb， 代表线程t2中的执行记录，此时t1的执行记录已经清除
        });
        Thread t2 = new Thread(() -> {
            interpreter.createUser("A");
            interpreter.interpret("b");
            interpreter.interpret("b");
            interpreter.interpret("his");
        });

        t1.start();
        /*
         * t1和t2谁先被启动有一定随机性，
         * 这里模拟t1先启动，并创建了用户A，但线程结束时并未销毁资源,
         * t2启动后拿到了用户A的资源，并可以查看到t1线程中执行的记录
         */
        Thread.sleep(100);
        t2.start();

        t1.join();
        t2.join();

        ClientCenter.show();
    }

    @Cmd
    public void a() {

    }

    @Cmd
    public void b() {

    }

}
