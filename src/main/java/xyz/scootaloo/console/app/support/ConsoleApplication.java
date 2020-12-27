package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.config.ApplicationConfig;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ConsoleApplication extends Colorful {

    public static void run(Object configObj) {
        if (!ClassUtils.isExtendForm(configObj, ApplicationConfig.class)) {
            println(red("启动类必须继承自配置类"));
            exit0();
        }
        ApplicationConfig config = (ApplicationConfig) configObj;
    }

}
