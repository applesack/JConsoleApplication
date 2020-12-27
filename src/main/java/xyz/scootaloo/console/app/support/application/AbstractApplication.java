package xyz.scootaloo.console.app.support.application;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 21:54
 */
public abstract class AbstractApplication {

    protected abstract Invoker findInvoker(String cmdName);

    protected abstract List<String> getInput();

    protected abstract void printPrompt();

    protected abstract boolean isExitCmd(String cmdName);

    protected void run() {
        welcome();
        while (true) {
            try {
                printPrompt();
                List<String> cmdItems = getInput();
                String cmdName = getCmdName(cmdItems);
                if (isExitCmd(cmdName))
                    break;
                Invoker invoker = findInvoker(cmdName);
                invoker.invoke(cmdItems);
            } catch (Exception e) {
                exceptionHandle(e);
            }
        }
        whenExit();
    }

    protected void welcome() {}

    protected void whenExit() {}

    protected void exceptionHandle(Exception e) {}

    public String getCmdName(List<String> items) {
        String cmdName = items.get(0);
        items.remove(0);
        return cmdName;
    }

}
