package xyz.scootaloo.console.app.support.application;

import xyz.scootaloo.console.app.support.parser.Actuator;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 21:54
 */
public abstract class AbstractApplication {

    protected abstract Actuator findInvoker(String cmdName);

    protected abstract String getInput();

    protected abstract void printPrompt();

    protected abstract boolean isExitCmd(String cmdName);

    protected void run() {
        welcome();
        while (true) {
            try {
                printPrompt();
                if (simpleRunCommand(getInput()))
                    break;
            } catch (Exception e) {
                exceptionHandle(e);
            }
        }
        whenExit();
    }

    protected void welcome() {}

    protected void whenExit() {}

    protected void exceptionHandle(Exception e) {
        e.printStackTrace();
    }

    protected boolean simpleRunCommand(String command) throws Exception {
        List<String> cmdItems = StringUtils.toList(command);
        String cmdName = getCmdName(cmdItems);
        if (isExitCmd(cmdName))
            return true;
        Actuator invoker = findInvoker(cmdName);
        invoker.invoke(cmdItems);
        return false;
    }

    public String getCmdName(List<String> items) {
        if (items.isEmpty())
            return "";
        String cmdName = items.get(0);
        items.remove(0);
        return cmdName.toLowerCase(Locale.ROOT);
    }

}
