package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.error.CommandInvokeException;
import xyz.scootaloo.console.app.parser.AssemblyFactory.MethodActuator;
import xyz.scootaloo.console.app.util.StringUtils;

import java.util.List;

/**
 * 解释器
 * @author flutterdash@qq.com
 * @since 2021/1/6 23:00
 */
public final class Interpreter {
    private final ConsoleConfig config;
    protected static InvokeInfo lastInvokeInfo;

    public Interpreter(ConsoleConfig config) {
        if (!AssemblyFactory.hasInit) {
            AssemblyFactory.init(config);
            AssemblyFactory.hasInit = true;
        }
        this.config = config;
    }

    /**
     * @param cmd 命令行
     * @return 方法调用信息
     */
    public InvokeInfo interpret(String cmd) {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Actuator actuator = AssemblyFactory.findActuator(cmdName);
        return actuator.invoke(allTheCmdItem);
    }

    /**
     * 根据方法名调用无参方法
     * @param name 框架容器中管理的无参方法的方法名
     * @return 调用信息
     */
    public InvokeInfo call(String name) {
        MethodActuator actuator = (MethodActuator) AssemblyFactory.findActuator(name);
        return actuator.invokeByArgs();
    }

    /**
     * 根据方法名，方法参数来调用方法<br>
     * 这个方法必须是容器管理的，否则无法调用
     * @param name 方法名
     * @param args 此方法需要的参数
     * @return 调用信息
     */
    public InvokeInfo call(String name, Object ... args) {
        Actuator actuator = AssemblyFactory.findActuator(name);
        if (actuator instanceof MethodActuator) {
            MethodActuator methodActuator = (MethodActuator) AssemblyFactory.findActuator(name);
            return methodActuator.invokeByArgs(args);
        } else {
            return InvokeInfo.failed(null, null, new CommandInvokeException("没有这个命令"));
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
