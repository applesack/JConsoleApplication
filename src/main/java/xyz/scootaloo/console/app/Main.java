package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.Opt;

import java.util.Random;

/**
 * 示例
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Main {

    /**
     * 启动控制台应用
     * @param args -
     */
    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

    /**
     * 命令是这样被调用的<br>
     * 尝试在控制台输入<br>
     * hello
     */
    @Cmd(parser = "sub")
    public void hello(Random random, @Opt(value = 'a') int a,
                                    @Opt(value = 'b', dftVal = "-23") int b) {
        System.out.println("hello world");
        System.out.println("a: " + a + ", b: " + b);
        System.out.println(random.nextInt());
    }

}
