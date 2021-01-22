package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.Actuator;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.AssemblyFactory.MethodActuator;
import xyz.scootaloo.console.app.utils.StringUtils;

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
        MethodActuator actuator = (MethodActuator) AssemblyFactory.findActuator(name);
        return actuator.invokeByArgs();
    }

    // 根据方法名调用有参方法，需要注入参数
    public InvokeInfo invoke(String name, Object ... args) {
        Actuator actuator = AssemblyFactory.findActuator(name);
        if (actuator instanceof MethodActuator) {
            MethodActuator methodActuator = (MethodActuator) AssemblyFactory.findActuator(name);
            return methodActuator.invokeByArgs(args);
        } else {
            return InvokeInfo.failed(null, null, new RuntimeException("没有这个命令"));
        }
    }

    /**
     * 将一个命令方法的返回值设置到指定的key上
     * @param key 键
     * @param cmd 命令
     * @return 是否设置成功
     */
    public boolean set(String key, String cmd) {
        interpret("set " + key);
        return interpret(cmd).isSuccess();
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
