package xyz.scootaloo.console.app.application;

import xyz.scootaloo.console.app.util.StringUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * 抽象的控制台应用
 * 执行 run() 方法后不断等待键盘输入，并解析处理
 * @author flutterdash@qq.com
 * @since 2020/12/27 21:54
 */
public abstract class AbstractConsoleApplication {

    // 提供一个异常处理器
    protected Consumer<Exception> exceptionHandle;
    protected ExitAction exitAction = () -> System.exit(0);

    // 获取字符串输入
    protected abstract String getInput();

    // 在获取输入之前的提示符
    protected abstract void printPrompt();

    // 判断一个命令是否是退出命令
    protected abstract boolean isExitCmd(String cmdName);

    /**
     * 1. 打印欢迎信息
     * 2. 等待键盘输入
     * 3. 解释键盘输入
     *      3.1 假如是退出命令，则执行 whenExit() 方法后应用退出
     *      3.2 解释命令时抛出异常，将由 exceptionHandle(e) 进行处理
     * 4. 解释结束，回到步骤2.
     */
    public void run() {
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
        shutdown();
    }

    // 欢迎信息
    protected void welcome() {}

    // 退出时调用
    protected void shutdown() {
        if (exitAction != null)
            exitAction.shutdown();
    }

    // 异常处理器
    protected void exceptionHandle(Exception e) {
        if (exceptionHandle != null) {
            exceptionHandle.accept(e);
        }
    }

    // setter---------------------------------------------------------------

    public void setExceptionHandle(Consumer<Exception> exceptionHandle) {
        this.exceptionHandle = exceptionHandle;
    }

    public void setExitAction(ExitAction exitAction) {
        this.exitAction = exitAction;
    }

    // ---------------------------------------------------------------------

    /**
     * 运行命令的方式
     * @param command 字符串命令
     * @return bool 是否是退出命令
     * @throws Exception 可能抛出的异常
     */
    abstract boolean simpleRunCommand(String command) throws Exception;

    // 仅获取第一个被空格分隔的字符段，以小写形式返回
    protected String getCmdName(List<String> items) {
        if (items.isEmpty())
            return "";
        String cmdName = items.get(0).trim();
        return StringUtils.customizeToLowerCase0(cmdName);
    }

    @FunctionalInterface
    public interface ExitAction {
        void shutdown();
    }

}
