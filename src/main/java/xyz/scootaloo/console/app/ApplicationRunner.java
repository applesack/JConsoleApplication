package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.application.AbstractApplication;
import xyz.scootaloo.console.app.application.ConsoleApplication;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.AssemblyFactory;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.util.ClassUtils;

/**
 * 应用运行器
 * 目前支持两种开始方式
 * 1. 启动一个控制台应用
 *      1.1 如果无参运行，默认将调用者实例化
 * 2. 获得一个解释器对象
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ApplicationRunner {
    // 单例: 命令解释器
    private static Interpreter INTERPRETER_SINGLETON;

    // 运行一个 ConsoleApplication
    public static AbstractApplication consoleApplication(ConsoleConfig config) {
        AssemblyFactory.init(config);
        return new ConsoleApplication(config, getInterpreter(config));
    }

    // 无参运行
    public static AbstractApplication consoleApplication() {
        Object instance = ClassUtils.instance(false);
        ConsoleConfig config = Console.config()
                .addCommandFactories()
                    .add(instance, true)
                    .ok().build();
        AssemblyFactory.init(config);
        return new ConsoleApplication(config, getInterpreter(config));
    }

    // 获取解释器对象，可以快捷的调用命令工厂的方法
    public static Interpreter getInterpreter(ConsoleConfig config) {
        if (INTERPRETER_SINGLETON == null) {
            INTERPRETER_SINGLETON = new Interpreter(config);
        }
        return INTERPRETER_SINGLETON;
    }

}
