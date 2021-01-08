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
        if (!AssemblyFactory.hasInit) {
            AssemblyFactory.init(config);
            AssemblyFactory.hasInit = true;
        }
        this.config = config;
    }

    public InvokeInfo interpret(String cmd) {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Actuator actuator = AssemblyFactory.findInvoker(cmdName);
        return actuator.invoke(allTheCmdItem);
    }

    public InvokeInfo invoke(String name, Object ... args) {
        AssemblyFactory.ActuatorImpl actuator = (AssemblyFactory.ActuatorImpl) AssemblyFactory.findInvoker(name);
        return actuator.invokeByArgs(args);
    }

    public InvokeInfo invoke(String name) {
        AssemblyFactory.ActuatorImpl actuator = (AssemblyFactory.ActuatorImpl) AssemblyFactory.findInvoker(name);
        return actuator.invokeByArgs();
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
