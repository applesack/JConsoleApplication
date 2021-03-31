package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.common.Console;

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
        ApplicationRunner.consoleApplication().run(args);
    }

    /**
     * 命令是这样被调用的<br>
     * 尝试在控制台输入<br>
     * hello
     */
    @Cmd(comment = "示例")
    public void hello(Console console) {
        console.println("hello world!!");
    }

}
