package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.application.AbstractConsoleApplication;
import xyz.scootaloo.console.app.application.ConsoleApplication;
import xyz.scootaloo.console.app.common.*;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.AssemblyFactory;
import xyz.scootaloo.console.app.parser.ExtraOptionHandle;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.util.ClassUtils;

import static xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

/**
 * 应用运行器<br>
 * <pre>
 * 目前支持两种开始方式
 * 1. 启动一个控制台应用
 *      1.1 如果无参运行，默认将调用者实例化
 * 2. 获得一个解释器对象 </pre>
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public final class ApplicationRunner {
    // 单例: 命令解释器
    private static Interpreter INTERPRETER_SINGLETON;

    /**
     * 使用一个配置对象启动获取控制台应用<br>
     * 框架中获取配置通常是使用java代码设置工厂，结合配置文件(classpath:/console.yml)<br>
     * @see Console#factories() 请参考示例代码中的配置方式
     * @param config 一个配置对象
     * @return 控制台应用对象
     */
    public static AbstractConsoleApplication consoleApplication(ConsoleConfig config) {
        AssemblyFactory.init(config);
        return new ConsoleApplication(config, getInterpreter(config));
    }

    /**
     * 无参运行<br>
     * 当调用{@code ApplicationRunner.consoleApplication()}时，框架会实例化调用者，将调用者做为工厂注册到框架。
     * @see Main 请参考默认的启动实例，这里使用了无参运行，这里{@code Main}这个类被实例化，其中{@link Main#hello()}这个方法被扫描到。
     * 默认配置文件，请参考 classpath:/console.yml
     * 假如 classpath下不包含这个文件，则使用构建者的默认配置 {@link DefaultValueConfigBuilder}
     * @return 基于默认配置生成的控制台应用
     */
    public static AbstractConsoleApplication consoleApplication() {
        Object instance = ClassUtils.instance(false);
        return consoleApplication(Console.factories()
                .add(instance, true)
                .ok());
    }

    /**
     * 根据配置生成解释器对象，此解释器可以使用字符串命令行执行所有注册到框架中的方法
     * @param config 一个配置
     * @return 解释器对象
     */
    public static Interpreter getInterpreter(ConsoleConfig config) {
        if (INTERPRETER_SINGLETON == null) {
            INTERPRETER_SINGLETON = new Interpreter(config);
            ExtraOptionHandle.setInterpreter(INTERPRETER_SINGLETON);
        }
        return INTERPRETER_SINGLETON;
    }

}
