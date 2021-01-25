package xyz.scootaloo.console.app.application;

import xyz.scootaloo.console.app.common.Colorful;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.listener.EventPublisher;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.InvokeInfo;
import xyz.scootaloo.console.app.util.StringUtils;

import java.util.List;
import java.util.Scanner;

/**
 * 对AbstractApplication的具体实现和补充
 * @see xyz.scootaloo.console.app.application.AbstractApplication
 * @author flutterdash@qq.com
 * @since 2020/12/27 23:44
 */
public class ConsoleApplication extends AbstractApplication {
    // resources
    private final Scanner scanner = ResourceManager.getScanner();
    private final Colorful console = ResourceManager.getColorfulPrinter();
    private final String prompt;
    private final ConsoleConfig config;
    private final Interpreter interpreter;

    /**
     * 需要以一个配置类和一个解释器做为生成此实例的条件
     * 生成对象时，会执行初始化命令
     * @param config 控制台应用的配置
     * @param interpreter 解释器
     */
    public ConsoleApplication(ConsoleConfig config, Interpreter interpreter) {
        this.config = config;
        this.interpreter = interpreter;
        this.prompt = getPrompt();

        // 设置默认的异常处理方式
        setExceptionHandle((e) -> console.onException(config, e));
        // 执行初始化命令
        doInit(config.getInitCommands());
    }

    private String getPrompt() {
        String prompt = config.getPrompt();
        if (!prompt.endsWith(" ")) {
            prompt += " ";
        }
        return prompt;
    }

    // 直接执行这些命令
    private void doInit(List<String> inits) {
        if (inits == null || inits.isEmpty())
            return;

        try {
            for (String cmd : inits) {
                simpleRunCommand(cmd);
            }
        } catch (Exception e) {
            console.onException(config, e, "初始化遇到异常", true);
        }
    }

    @Override // 从键盘获取输入
    protected String getInput() {
        String cmdline = scanner.nextLine().trim();
        cmdline = EventPublisher.onInput(cmdline);
        return cmdline;
    }

    @Override // 提示符从配置中获取
    protected void printPrompt() {
        console.print(console.grey(this.prompt));
    }

    @Override // 从配置中判断是否是退出命令
    protected boolean isExitCmd(String cmdName) {
        for (String cmd : this.config.getExitCmd()) {
            if (cmd == null || cmd.equals(cmdName))
                return true;
        }
        return false;
    }

    /**
     * 处理字符串命令的方式
     * 1. 将字符串命令按照空格分隔，切割成列表
     * 2. 获取命令名(列表的第一个元素)是否是退出命令
     * 3. 交给解释器执行，解释器将返回调用结果
     * 4. 调用如果失败则输出调用信息
     *
     * @param command 字符串命令
     * @return 是否是退出命令
     */
    @Override
    boolean simpleRunCommand(String command) {
        List<String> cmdItems = StringUtils.toList(command);
        String cmdName = getCmdName(cmdItems);
        if (isExitCmd(cmdName))
            return true;
        InvokeInfo info = interpreter.interpret(command);
        if (!info.isSuccess()) {
            if (config.isPrintStackTraceOnException()) {
                info.getException().printStackTrace();
            } else {
                console.println(info.getExMsg());
            }
        }
        return false;
    }

}
