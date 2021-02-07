package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.common.OutPrinter;

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

    @Cmd
    public void test(OutPrinter outPrinter) throws InterruptedException {
        outPrinter.println("方法开始");
        Thread.sleep(2000);
        outPrinter.println("方法结束");
    }

}
