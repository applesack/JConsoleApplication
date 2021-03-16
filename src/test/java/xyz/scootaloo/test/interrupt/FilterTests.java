package xyz.scootaloo.test.interrupt;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.CmdType;
import xyz.scootaloo.console.app.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/5 22:38
 */
public class FilterTests {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

    @Test
    public void testThread() throws InterruptedException {
        Interpreter interpreter = ApplicationRunner.getInterpreter();
        new Thread(() -> {
            interpreter.interpret("hello hello1");
            interpreter.interpret("hello hello2");
        }, "A").start();
        Thread.sleep(100);
        new Thread(() -> {
            interpreter.interpret("hello-Vas thank");
        }, "B").start();
    }

    @Cmd
    public void hello() {
    }

    @Cmd(type = CmdType.Filter)
    public boolean filter(String command) {
        System.out.println(Thread.currentThread().getName() +  " Filter: " + command);
        return true;
    }

}
