package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.application.AbstractConsoleApplication;
import xyz.scootaloo.console.app.application.ConsoleApplication;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.util.ClassUtils;

import static xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

/**
 * 功能入口
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
@Stateless
public final class ApplicationRunner {

    /**
     * 使用一个配置对象启动并获取控制台应用对象<br>
     * 框架中获取配置通常是使用java代码设置工厂，结合配置文件(classpath:/console.yml)<br>
     * @param config 一个配置对象
     * @return 控制台应用对象
     */
    public static AbstractConsoleApplication consoleApplication(ConsoleConfig config) {
        return new ConsoleApplication(config, getInterpreter(config));
    }

    /**
     * 无参运行<br>
     * 当调用此方法时，框架会实例化调用者，将调用者做为工厂注册到框架。
     * @return 基于默认配置生成的控制台应用
     */
    public static AbstractConsoleApplication consoleApplication() {
        Object boot = ClassUtils.instance(false);
        DefaultValueConfigBuilder configBuilder = Console.factories()
                .add(boot, true).then();
        supplementConfig(configBuilder, boot);
        return consoleApplication(configBuilder.build());
    }

    private static void supplementConfig(DefaultValueConfigBuilder builder, Object bootObj) {
        if (bootObj == null)
            return;
        Class<?> bootClazz = bootObj.getClass();
        Boot boot = bootClazz.getAnnotation(Boot.class);
        if (boot != null && !boot.name().isEmpty()) {
            String prompt = boot.name();
            if (Character.isLetter(prompt.charAt(prompt.length() - 1)))
                prompt += ">";
            builder.prompt(prompt);
        }
    }

    /**
     * 无参获取解释器，默认将调用者实例化，将调用者做为工厂使用
     * @return 解释器对象
     */
    public static Interpreter getInterpreter() {
        Object instance = ClassUtils.instance(false);
        return getInterpreter(Console.factories()
                .add(instance, true)
                .ok());
    }

    /**
     * 根据配置生成解释器对象，此解释器可以使用字符串命令行执行所有注册到框架中的方法
     * @param config 一个配置
     * @return 解释器对象
     */
    public static Interpreter getInterpreter(ConsoleConfig config) {
        return Interpreter.getInstance(config);
    }

}
