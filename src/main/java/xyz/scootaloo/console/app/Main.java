package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.Cmd;

/**
 * 默认的启动
 * 只包含一个 hello 命令方法，以及一些系统命令
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Main {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

    @Cmd
    private void hello() {
        System.out.println("hello world");
    }

}
