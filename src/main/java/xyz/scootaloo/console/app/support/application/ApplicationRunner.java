package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.AssemblyFactory;
import xyz.scootaloo.console.app.support.parser.Interpreter;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:04
 */
public class ApplicationRunner {

    private static Interpreter INTERPRETER_SINGLETON;

    public static void consoleApplication(ConsoleConfig config) {
        AssemblyFactory.init(config);
        new ConsoleApplication(config, getInstance(config)).run();
    }

    private static Interpreter getInstance(ConsoleConfig config) {
        if (INTERPRETER_SINGLETON == null) {
            INTERPRETER_SINGLETON = new Interpreter(config);
        }
        return INTERPRETER_SINGLETON;
    }

}
