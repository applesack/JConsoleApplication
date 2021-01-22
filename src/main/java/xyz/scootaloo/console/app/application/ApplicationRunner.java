package xyz.scootaloo.console.app.application;

import xyz.scootaloo.console.app.Console;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.AssemblyFactory;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public abstract class ApplicationRunner {
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
