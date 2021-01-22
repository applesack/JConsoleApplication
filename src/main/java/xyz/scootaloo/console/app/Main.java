package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.application.ApplicationRunner;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Main {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

}
