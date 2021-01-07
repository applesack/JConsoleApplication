package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ProxyInvoke;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.config.Author;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.TransformFactory.ResultWrapper;
import xyz.scootaloo.console.app.support.plugin.ConsolePlugin;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 装配工厂
 * 系统启动后扫描指定的包下所有类文件，按照一定逻辑进行装配
 * @author flutterdash@qq.com
 * @since 2020/12/28 10:05
 */
public class AssemblyFactory {
    private static final Colorful cPrint = ResourceManager.cPrint;
    protected static final Map<String, Actuator> strategyMap = new HashMap<>();
    protected static final List<ActuatorImpl> initActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> preActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> destroyActuators = new ArrayList<>();

    private static ConsoleConfig config;
    protected static boolean hasInit = false;

    private static final List<String> EMPTY_CMD_ITEMS = new ArrayList<>();

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
            return InvokeInfo.failed(Void.class, EMPTY_CMD_ITEMS,
                    new RuntimeException("当前这个命令是一个空命令"));
        };
    }

    private static void doGetStrategyFactories() {
        if (config == null) {
            cPrint.exit0("未加载到配置");
            return;
        }
        hasInit = true;
        welcome();
        Set<Class<?>> factories = new LinkedHashSet<>(config.getFactories());
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
            case Parser: {
                doGetParser(method, cmdAnno, o);
            }
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

    private static void doGetParser(Method method, Cmd cmdAnno, Object o) {
        if (cmdAnno.targets().length == 0)
            return;
        Class<?>[] types = cmdAnno.targets();
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || params[0] != String.class)
            return;
        if (method.getReturnType() == void.class || method.getReturnType() == Void.class)
            return;
        Function<String, Object> parserFunc = (str) -> {
            try {
                method.setAccessible(true);
                return method.invoke(o, str);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (config.isPrintStackTraceOnException())
                    e.printStackTrace();
                else
                    cPrint.println(e.getMessage());
                return null;
            }
        };
        ResolveFactory.addParser(parserFunc, types);
    }

    private static void welcome() {
        if (!config.isPrintWelcome())
            return;
        Author author = config.getAuthor();
        cPrint.println(":: " + config.getAppName() + " ::");
        cPrint.println("author: " + author.getName());
        cPrint.println("email: " + author.getEmail());
        cPrint.println("create since: " + author.getCreateDate());
        cPrint.println("last update: " + author.getUpdateDate());
        cPrint.println(author.getComment());
        cPrint.println("欢迎使用");
    }

    /**
     * @author flutterdash@qq.com
     * @since 2020/12/29 11:00
     */
    public static class ActuatorImpl implements Actuator {

        private final Method method;
        private final Cmd cmd;
        private final Object obj;

        private final Class<?> rtnType;
        private final String cmdName;

        public ActuatorImpl(Method m, Cmd c, Object o) {
            this.cmd = c;
            this.method = m;
            this.obj = o;

            this.cmdName = method.getName().toLowerCase(Locale.ROOT);
            this.rtnType = method.getReturnType();
        }

        @Override
        public InvokeInfo invoke(List<String> items) {
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
                        return InvokeInfo.failed(rtnType, items,
                                new RuntimeException("前置方法执行未通过"));
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
                        if (!(rtnType.equals(boolean.class) ||
                                rtnType.equals(Boolean.class)))
                            return false;
                    }
                } break;
            }
            return true;
        }

        private boolean doInvokePreProcess() {
            for (ActuatorImpl actuator : preActuators) {
                InvokeInfo info = actuator.invoke0(null);
                if (!info.isSuccess()) {
                    String msg = "错误信息: " + actuator.cmd.onError();
                    if (info.getException() != null)
                        msg = info.getExMsg();
                    cPrint.println(msg);
                    if (config.isPrintStackTraceOnException())
                        info.getException().printStackTrace();
                    return false;
                } else {
                    boolean result = (boolean) info.get();
                    if (!result)
                        return false;
                }
            }
            return true;
        }

        private InvokeInfo invoke0(List<String> items) {
            InvokeInfo info = InvokeInfo.beforeInvoke(rtnType, items);
            EventPublisher.onResolveInput(cmdName, items);
            ResultWrapper wrapper = TransformFactory.transform(method, items);
            if (wrapper.success) {
                method.setAccessible(true);
                Object rtnVal = null;
                try {
                    rtnVal = method.invoke(obj, wrapper.args);
                    info.finishInvoke(rtnVal, wrapper.args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    info.onException(e, wrapper.args);
                }
                EventPublisher.onInputResolved(cmdName, rtnVal);
            } else {
                EventPublisher.onInputResolved(cmdName,null);
                info.onException(wrapper.ex, null);
            }
            return info;
        }

        public int getOrder() {
            return cmd.order();
        }

        public void printInfo() {

        }

    }
}
