package xyz.scootaloo.test.thread;

import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.client.ClientCenter;
import xyz.scootaloo.console.app.client.ResourcesHandler;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 10:24
 */
public class ThreadTest {

    private static final String[][] commands = {
            {"a", "b", "c"},
            {"c", "c"},
            {"c", "a", "a"},
            {"b", "b", "c", "a"}
    };

    public static void main(String[] args) throws InterruptedException {
        final int threadNumber = commands.length;
        final CountDownLatch cdl = new CountDownLatch(threadNumber);
        final Interpreter interpreter = ApplicationRunner.getInterpreter();
        final Map<String, List<InvokeInfo>> hisMap = new ConcurrentHashMap<>();

        Thread[] threads = new Thread[threadNumber];
        for (int i = 0; i<threadNumber; i++) {
            int finalRow = i;
            threads[i] = new Thread(() -> {
                String userKey = String.valueOf(finalRow);
                // 创建一个用户，获得一个销毁用户资源的回调
                ResourcesHandler resourcesHandler = interpreter.setUser(userKey);
                for (int j = 0; j<commands[finalRow].length; j++)
                  interpreter.interpret(commands[finalRow][j]);
                // 当此线程结束后，此用户的资源将被销毁
                resourcesHandler.shutdown();
                // 历史记录已经被清除，所以his得到的是一个空的列表
                List<InvokeInfo> his = interpreter.interpret("his").get();
                hisMap.put(userKey, his);
               cdl.countDown();
            });
        }

        for (Thread thread : threads)
            thread.start();

        cdl.await();
        // 通过debug观察各个连接的信息
        System.out.println("PAUSE");
        ClientCenter.show();
    }

    @Cmd
    public void a(StringPool pool) {

    }

    @Cmd
    public void b(StringPool pool) {

    }

    @Cmd
    public void c(StringPool pool) {

    }

    private static class StringPool {
        private final StringBuilder pool;
        public StringPool() {
            this.pool = new StringBuilder();
        }

        public void write(String content) {
            synchronized (this) {
                pool.append(content).append('\n');
            }
        }

        public void show() {
            System.out.println(pool.toString());
        }

    }

}
