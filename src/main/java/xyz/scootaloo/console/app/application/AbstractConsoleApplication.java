package xyz.scootaloo.console.app.application;

import xyz.scootaloo.console.app.application.processor.PostProcessor;
import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;
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
    protected Consumer<ConsoleAppRuntimeException> exceptionHandle;
    // 默认退出动作，调用 System.exit(0) 退出程序
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
            } catch (ConsoleAppRuntimeException e) {
                exceptionHandle(e);
            } catch (Exception otherException) {
                otherException.printStackTrace();
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
    protected void exceptionHandle(ConsoleAppRuntimeException e) {
        if (exceptionHandle != null) {
            exceptionHandle.accept(e);
        }
    }

    // setter---------------------------------------------------------------

    /**
     * 提供一个异常处理器<br>
     * <p>框架中的异常分为两类，<br>
     *     1. 解析命令行时<br>
     *     2. 执行方法时</p>
     * @see ConsoleAppRuntimeException
     * @param exceptionHandle 异常处理器
     * @return 返回控制台应用对象，可以继续进行配置
     */
    public AbstractConsoleApplication setExceptionHandle(Consumer<ConsoleAppRuntimeException> exceptionHandle) {
        if (exceptionHandle != null)
            this.exceptionHandle = exceptionHandle;
        return this;
    }

    /**
     * 指定一个其他的退出方法
     * @param exitAction 这将是系统的最后一个方法，调用完这个方法后，程序退出。
     * @return 返回控制台应用对象，可以继续进行配置
     */
    public AbstractConsoleApplication setExitAction(ExitAction exitAction) {
        this.exitAction = exitAction;
        return this;
    }

    /**
     * 提供一个处理器接受命令的执行信息
     * @param processor 处理器
     * @return 返回控制台应用对象，可以继续进行配置，另外，这个处理器可以设置多个，框架将按照顺序调用它们
     */
    public abstract AbstractConsoleApplication addPostProcessor(PostProcessor processor);

    // ---------------------------------------------------------------------

    /**
     * 运行命令的方式
     * @param command 字符串命令
     * @return bool 是否是退出命令
     * @throws ConsoleAppRuntimeException 可能抛出的异常
     */
    protected abstract boolean simpleRunCommand(String command) throws ConsoleAppRuntimeException;

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
