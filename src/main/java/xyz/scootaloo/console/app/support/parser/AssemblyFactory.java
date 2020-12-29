package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ProxyInvoke;
import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.CmdType;
import xyz.scootaloo.console.app.support.component.StrategyFactory;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
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
    private static Map<String, Actuator> strategyMap = null;
    private static final List<ActuatorImpl> initActuators = new ArrayList<>();
    private static final List<ActuatorImpl> preActuators = new ArrayList<>();
    private static final List<ActuatorImpl> destroyActuators = new ArrayList<>();

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
        cPrint.println(cPrint.blue("没有这个命令`" + cmdName + "`"));
        return cmd -> {
            // do nothing ...
            return null;
        };
    }

    private static void doGetStrategyFactories() {
        strategyMap = new HashMap<>();
        if (config == null) {
            cPrint.exit0("未加载到配置");
            return;
        }
        Set<Class<?>> factories = PackScanner.getClasses(config.getBasePack());
        for (Class<?> factory : factories) {
            StrategyFactory factoryAnno = factory.getAnnotation(StrategyFactory.class);
            if (factoryAnno == null || !factoryAnno.enable())
                continue;
            Object instance = ProxyInvoke.invoke(factory);
            Method[] methods = factory.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (cmd == null)
                    continue;
                doResolveCmd(method, cmd, instance);
            }
        }

        strategyMap.put("help", help());
        sortActuatorLists();

        List<String> emptyList = new ArrayList<>(0);
        try {
            for (ActuatorImpl actuator : initActuators) {
                actuator.invoke0(emptyList);
            }
        } catch (Exception e) {
            cPrint.exit0("初始化失败, msg:" + e.getMessage());
        }
    }

    private static void doResolveCmd(Method method, Cmd cmdAnno, Object o) {
        ActuatorImpl actuator = new ActuatorImpl(method, cmdAnno, o);
        if (!actuator.checkMethod())
            return;
        switch (cmdAnno.type()) {
            case Cmd: {
                strategyMap.put(method.getName(), actuator);
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

    private static Actuator help() {
        return items -> {
            if (items.isEmpty()) {
                for (Actuator actuator : strategyMap.values()) {
                    printInfo(actuator);
                }
            } else {
                Actuator actuator = findInvoker(items.get(0));
                printInfo(actuator);
            }
            return null;
        };
    }

    private static void printInfo(Actuator actuator) {
        if (actuator instanceof ActuatorImpl) {
            ((ActuatorImpl) actuator).printInfo();
        }
    }

    private static void sortActuatorLists() {
        initActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        preActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        destroyActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
    }

    /**
     * @author flutterdash@qq.com
     * @since 2020/12/29 11:00
     */
    public static class ActuatorImpl implements Actuator {
        private static final List<String> EMPTY_ARGS = new ArrayList<>(0);

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
                Boolean result = (Boolean) actuator.invoke0(EMPTY_ARGS);
                if (!result) {
                    cPrint.println("错误信息: " + actuator.cmd.onError());
                }
            }
            return true;
        }

        private Object invoke0(List<String> items) throws InvocationTargetException, IllegalAccessException {
            Object[] args = TransformFactory.transform(method, items);
            method.setAccessible(true);
            return method.invoke(obj, args);
        }

        public int getOrder() {
            return cmd.order();
        }

        public void printInfo() {

        }

    }
}
