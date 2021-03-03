package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.mark.Boot;
import xyz.scootaloo.console.app.common.CPrinter;

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
    @Cmd
    public void hello(CPrinter printer) {
        printer.println("hello world!!");
    }

    @Cmd
    public void xx() {

    }

}
