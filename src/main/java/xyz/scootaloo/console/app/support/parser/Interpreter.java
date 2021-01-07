package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/6 23:00
 */
public class Interpreter {

    private final ConsoleConfig config;

    public Interpreter(ConsoleConfig config) {
        this.config = config;
        if (!AssemblyFactory.hasInit)
            throw new RuntimeException("装配工厂未初始化");
    }

    public InvokeInfo interpretation(String cmd) throws Exception {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Actuator actuator = AssemblyFactory.findInvoker(cmdName);
        return actuator.invoke(allTheCmdItem);
    }

    public boolean isExitCmd(String cmd) {
        return false;
    }

    public ConsoleConfig getConfig() {
        return this.config;
    }

    private String getCmdName(List<String> items) {
        if (items.isEmpty()) {
            return null;
        } else {
            String cmdName = items.remove(0).trim();
            return cmdName.isEmpty() ? null : cmdName;
        }
    }

}
