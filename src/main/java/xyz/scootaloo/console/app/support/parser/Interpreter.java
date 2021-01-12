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
    protected static InvokeInfo lastInvokeInfo;

    public Interpreter(ConsoleConfig config) {
        if (!AssemblyFactory.hasInit) {
            AssemblyFactory.init(config);
            AssemblyFactory.hasInit = true;
        }
        this.config = config;
    }

    // 执行命令
    public InvokeInfo interpret(String cmd) {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Actuator actuator = AssemblyFactory.findActuator(cmdName);
        return actuator.invoke(allTheCmdItem);
    }

    // 根据方法名调用无参方法
    public InvokeInfo invoke(String name) {
        AssemblyFactory.MethodActuator actuator = (AssemblyFactory.MethodActuator) AssemblyFactory.findActuator(name);
        return actuator.invokeByArgs();
    }

    // 根据方法名调用有参方法，需要注入参数
    public InvokeInfo invoke(String name, Object ... args) {
        AssemblyFactory.MethodActuator actuator = (AssemblyFactory.MethodActuator) AssemblyFactory.findActuator(name);
        return actuator.invokeByArgs(args);
    }

    // 获取当前解释器的配置对象
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
