package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.ResourceManager;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.Interpreter;
import xyz.scootaloo.console.app.support.parser.InvokeInfo;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.util.List;
import java.util.Scanner;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 23:44
 */
public class ConsoleApplication extends AbstractApplication implements Colorful {

    private final Scanner scanner = ResourceManager.scanner;
    private final ConsoleConfig config;
    private final Interpreter interpreter;

    public ConsoleApplication(ConsoleConfig config, Interpreter interpreter) {
        this.config = config;
        this.interpreter = interpreter;
        doInit(config.getInitCommands());
    }

    private void doInit(List<String> inits) {
        if (inits == null || inits.isEmpty())
            return;

        try {
            for (String cmd : inits) {
                simpleRunCommand(cmd);
            }
        } catch (Exception e) {
            if (config.isPrintStackTraceOnException()) {
                e.printStackTrace();
            } else {
                println(e.getMessage());
                exit0("初始化时遇到异常");
            }
        }
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

    @Override
    boolean simpleRunCommand(String command) throws Exception {
        List<String> cmdItems = StringUtils.toList(command);
        String cmdName = getCmdName(cmdItems);
        if (isExitCmd(cmdName))
            return true;
        InvokeInfo info = interpreter.interpretation(command);
        if (!info.isSuccess()) {
            if (config.isPrintStackTraceOnException()) {
                info.getException().printStackTrace();
            } else {
                println(info.getExMsg());
            }
        }
        return false;
    }
}
