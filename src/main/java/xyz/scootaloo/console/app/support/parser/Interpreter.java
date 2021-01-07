package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/6 23:00
 */
public class Interpreter {

    private final ConsoleConfig config;

    public Interpreter(ConsoleConfig config) {
        if (!AssemblyFactory.hasInit)
            throw new RuntimeException("装配工厂未初始化");
        this.config = config;
    }

    public InvokeInfo interpretation(String cmd) throws Exception {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Actuator actuator = AssemblyFactory.findInvoker(cmdName);
        return actuator.invoke(allTheCmdItem);
    }

    public ConsoleConfig getConfig() {
        return this.config;
    }

    private String getCmdName(List<String> items) {
        if (items.isEmpty()) {
            return "";
        } else {
            return items.remove(0).trim();
        }
    }

}
