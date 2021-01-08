package xyz.scootaloo.console.app.support.application;

import java.util.List;
import java.util.Locale;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 21:54
 */
public abstract class AbstractApplication {
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
        whenExit();
    }

    // 欢迎信息
    protected void welcome() {}

    // 退出时调用
    protected void whenExit() {}

    // 异常处理器
    protected void exceptionHandle(Exception e) {
        e.printStackTrace();
    }

    /**
     * 运行命令的方式
     * @param command 字符串命令
     * @return bool 是否是退出命令
     * @throws Exception 可能抛出的异常
     */
    abstract boolean simpleRunCommand(String command) throws Exception;

    // 仅获取第一个被空格分隔的字符段，以小写形式返回
    public String getCmdName(List<String> items) {
        if (items.isEmpty())
            return "";
        String cmdName = items.get(0).trim();
        return cmdName.toLowerCase(Locale.ROOT);
    }

}
