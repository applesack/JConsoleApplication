package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.AssemblyFactory;
import xyz.scootaloo.console.app.support.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public abstract class ApplicationRunner {

    // 单例
    private static Interpreter INTERPRETER_SINGLETON;

    // 运行一个 ConsoleApplication
    public static void consoleApplication(ConsoleConfig config) {
        AssemblyFactory.init(config);
        new ConsoleApplication(config, getInterpreter(config)).run();
    }

    // 获取解释器对象，可以快捷的调用命令工厂的方法
    public static Interpreter getInterpreter(ConsoleConfig config) {
        if (INTERPRETER_SINGLETON == null) {
            INTERPRETER_SINGLETON = new Interpreter(config);
        }
        return INTERPRETER_SINGLETON;
    }

}
