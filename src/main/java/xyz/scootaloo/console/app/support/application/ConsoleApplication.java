package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.config.ConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.Objects;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ConsoleApplication extends Colorful {

    public static void run(ConfigProvider provider) {
        Objects.requireNonNull(provider);
        ConsoleConfig config = provider.getConfig();
        println(config);
    }

}
