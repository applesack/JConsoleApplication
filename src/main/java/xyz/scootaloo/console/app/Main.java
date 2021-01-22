package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.application.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Boot;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Main {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication(
                Console.factories()
                        .addFactories()
                        .ok()
                .build()
        ).run();
    }

}
