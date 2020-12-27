package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.config.ApplicationConfig;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ConsoleApplication extends Colorful {

    public static void run(ApplicationConfig applicationConfig) {
        ConsoleConfig config = applicationConfig.getConfig();
        println(config);
    }

}
