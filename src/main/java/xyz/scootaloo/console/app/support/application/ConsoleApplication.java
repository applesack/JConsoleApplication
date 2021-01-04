package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.ResourceManager;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.Actuator;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;

import java.util.Scanner;
import java.util.function.Function;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 23:44
 */
public class ConsoleApplication extends AbstractApplication implements Colorful {

    private final Scanner scanner = ResourceManager.scanner;
    private final ConsoleConfig config;
    private final Function<String, Actuator> cmdFactory;

    public ConsoleApplication(ConsoleConfig config, Function<String, Actuator> cmdFactory,
                              String[] initCommands) {
        this.config = config;
        this.cmdFactory = cmdFactory;

        doInit(initCommands);
    }

    private void doInit(String[] inits) {
        if (inits == null || inits.length == 0)
            return;
        try {
            for (String cmd : inits) {
                simpleRunCommand(cmd);
            }
        } catch (Exception e) {
            if (config.isPrintStackTraceOnException())
                e.printStackTrace();
            else
                println(e.getMessage());
                exit0("初始化时遇到异常");
        }

    }

    @Override
    protected Actuator findInvoker(String cmdName) {
        return cmdFactory.apply(cmdName);
    }

    @Override
    protected String getInput() {
        String cmdline = scanner.nextLine().trim();
        cmdline = EventPublisher.onInput(cmdline);
        return cmdline;
    }

    @Override
    protected void printPrompt() {
        print(grey(this.config.getPrompt()));
    }

    @Override
    protected boolean isExitCmd(String cmdName) {
        for (String cmd : this.config.getExitCmd()) {
            if (cmd == null || cmd.equals(cmdName))
                return true;
        }
        return false;
    }

    @Override
    protected void exceptionHandle(Exception e) {
        if (config.isPrintStackTraceOnException())
            e.printStackTrace();
        else
            println(e.getMessage());
    }
}
