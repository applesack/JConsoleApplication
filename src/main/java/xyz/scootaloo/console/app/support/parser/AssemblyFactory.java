package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ProxyInvoke;
import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.CmdType;
import xyz.scootaloo.console.app.support.component.CommandFactory;
import xyz.scootaloo.console.app.support.component.Plugin;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.TransformFactory.ResultWrapper;
import xyz.scootaloo.console.app.support.plugin.ConsolePlugin;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;
import xyz.scootaloo.console.app.support.utils.ClassUtils;
import xyz.scootaloo.console.app.support.utils.PackScanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 10:05
 */
public class AssemblyFactory {
    private static final Colorful cPrint = Colorful.instance;
    protected static final Map<String, Actuator> strategyMap = new HashMap<>();
    protected static final List<ActuatorImpl> initActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> preActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> destroyActuators = new ArrayList<>();

    private static ConsoleConfig config;

    public static void init(ConsoleConfig conf) {
        config = conf;
        doGetStrategyFactories();
    }

    public static Actuator findInvoker(String cmdName) {
        cmdName = cmdName.toLowerCase(Locale.ROOT);
        Actuator actuator = strategyMap.get(cmdName);
        if (actuator != null)
            return actuator;
        if (!cmdName.equals(""))
            cPrint.println(cPrint.blue("没有这个命令`" + cmdName + "`"));
        return cmd -> {
            // do nothing ...
            return null;
        };
    }

    private static void doGetStrategyFactories() {
        if (config == null) {
            cPrint.exit0("未加载到配置");
            return;
        }
        Set<Class<?>> factories = PackScanner.getClasses(config.getBasePack());
        factories.add(SystemPresetCmd.class);
        for (Class<?> factory : factories) {
            CommandFactory factoryAnno = factory.getAnnotation(CommandFactory.class);
            Plugin plugin = factory.getAnnotation(Plugin.class);
            Object instance = ProxyInvoke.invoke(factory);
            if (plugin != null && plugin.enable())
                doGetPlugin(instance);
            if (factoryAnno == null || !factoryAnno.enable())
                continue;
            Method[] methods = factory.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (cmd == null)
                    continue;
                doResolveCmd(method, cmd, instance);
            }
        }

        EventPublisher.onAppStarted(config);
        sortActuatorLists();

        try {
            for (ActuatorImpl actuator : initActuators) {
                actuator.invoke0(null);
            }
        } catch (Exception e) {
            cPrint.exit0("初始化失败, msg:" + e.getMessage() + "\n");
            e.printStackTrace();
        }

        // 将销毁方法注入到系统关闭钩子中
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                for (Actuator actuator : destroyActuators) {
                    actuator.invoke(null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private static void doResolveCmd(Method method, Cmd cmdAnno, Object o) {
        ActuatorImpl actuator = new ActuatorImpl(method, cmdAnno, o);
        if (!actuator.checkMethod())
            return;
        switch (cmdAnno.type()) {
            case Cmd: {
                strategyMap.put(method.getName().toLowerCase(Locale.ROOT), actuator);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (!cmd.name().equals("")) {
                    strategyMap.put(cmd.name().toLowerCase(Locale.ROOT), actuator);
                }
            } break;
            case Pre: {
                preActuators.add(actuator);
            } break;
            case Init: {
                initActuators.add(actuator);
            } break;
            case Destroy: {
                destroyActuators.add(actuator);
            } break;
        }
    }

    private static void sortActuatorLists() {
        initActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        preActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        destroyActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
    }

    private static void doGetPlugin(Object pluginObj) {
        if (ClassUtils.isExtendForm(pluginObj, ConsolePlugin.class)) {
            EventPublisher.loadPlugin((ConsolePlugin) pluginObj);
        } else {
            cPrint.println("插件类使用了@Plugin注解，但是没有继承自ConsolePlugin接口，自定义插件无法装配");
        }
    }

    /**
     * @author flutterdash@qq.com
     * @since 2020/12/29 11:00
     */
    public static class ActuatorImpl implements Actuator {

        private final Method method;
        private final Cmd cmd;
        private final Object obj;

        public ActuatorImpl(Method m, Cmd c, Object o) {
            this.cmd = c;
            this.method = m;
            this.obj = o;
        }

        @Override
        public Object invoke(List<String> items) throws InvocationTargetException, IllegalAccessException {
            switch (cmd.type()) {
                case Destroy:
                case Pre:
                case Init: {
                    return invoke0(items);
                }
                default: {
                    if (doInvokePreProcess()) {
                        return invoke0(items);
                    } else {
                        return null;
                    }
                }
            }
        }

        public boolean checkMethod() {
            CmdType type = cmd.type();
            switch (type) {
                case Destroy:
                case Init:
                case Pre: {
                    if (method.getParameterCount() != 0)
                        return false;
                    if (type == CmdType.Pre) {
                        if (!(method.getReturnType().equals(boolean.class) ||
                                method.getReturnType().equals(Boolean.class)))
                            return false;
                    }
                } break;
            }
            return true;
        }

        private boolean doInvokePreProcess() throws InvocationTargetException, IllegalAccessException {
            for (ActuatorImpl actuator : preActuators) {
                Boolean result = (Boolean) actuator.invoke0(null);
                if (!result) {
                    cPrint.println("错误信息: " + actuator.cmd.onError());
                    return false;
                }
            }
            return true;
        }

        private Object invoke0(List<String> items) throws InvocationTargetException, IllegalAccessException {
            String methodName = method.getName().toLowerCase(Locale.ROOT);
            EventPublisher.onResolveInput(methodName, items);
            ResultWrapper wrapper = TransformFactory.transform(method, items);
            if (wrapper.success) {
                method.setAccessible(true);
                Object rtnVal = method.invoke(obj, wrapper.args);
                EventPublisher.onInputResolved(methodName, rtnVal);
                return rtnVal;
            } else {
                EventPublisher.onInputResolved(methodName, null);
                cPrint.println("调用失败: " + wrapper.msg);
                return null;
            }
        }

        public int getOrder() {
            return cmd.order();
        }

        public void printInfo() {

        }

    }
}
