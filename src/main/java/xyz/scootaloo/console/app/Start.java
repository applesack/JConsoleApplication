package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.ConsoleApplication;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.config.ApplicationConfig;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Start extends ApplicationConfig {

    public static void main(String[] args) {
        ConsoleApplication.run(instance());
    }

    @Override
    public ConsoleConfig register(DefaultValueConfigBuilder configBuilder) {
        return null;
    }
}
