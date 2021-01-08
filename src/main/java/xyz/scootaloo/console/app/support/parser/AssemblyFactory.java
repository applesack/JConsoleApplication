package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ProxyInvoke;
import xyz.scootaloo.console.app.support.common.ResourceManager;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.config.Author;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.parser.ResolveFactory.ResultWrapper;
import xyz.scootaloo.console.app.support.listener.AppListener;
import xyz.scootaloo.console.app.support.listener.EventPublisher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * 装配工厂
 * 扫描配置中的命令工厂类，装配其中的接口实现，带有注解的方法等
 * @author flutterdash@qq.com
 * @since 2020/12/28 10:05
 */
public class AssemblyFactory {
    // resources
    private static final Colorful cPrint = ResourceManager.cPrint;
    protected static final Map<String, Actuator> strategyMap = new HashMap<>();
    protected static final List<ActuatorImpl> initActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> preActuators = new ArrayList<>();
    protected static final List<ActuatorImpl> destroyActuators = new ArrayList<>();

    // config
    private static ConsoleConfig config;
    protected static boolean hasInit = false;

    // 进行初始化
    public static void init(ConsoleConfig conf) {
        config = conf;
        doGetStrategyFactories();
    }

    /**
     * 获取于此命令名对应的执行器对象
     * 当命令是空的，返回的是执行成功但没有任何内容的结果
     * @param cmdName 命令名，或者方法名
     * @return 执行器对象
     */
    public static Actuator findInvoker(String cmdName) {
        cmdName = cmdName.toLowerCase(Locale.ROOT);
        Actuator actuator = strategyMap.get(cmdName);
        if (actuator != null)
            return actuator;
        if (!cmdName.equals(""))
            cPrint.println(cPrint.blue("没有这个命令`" + cmdName + "`"));
        return cmd -> {
            // do nothing ...
            return InvokeInfo.simpleSuccess();
        };
    }

    /**
     * 执行装配工厂的初始化
     * 1. 遍历所有的工厂类
     * 2. 假如当前工厂类也实现的监听器的接口，那么当作监听器进行装配
     * 3. 遍历当前类的所有方法，假如含有 @Cmd 注解则进行对应的处理
     *
     * 4. 装配完成，将所有的 init 方法排序后调用
     * 5. 将 destroy 方法注册到关闭钩子上
     */
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
            Object instance = ProxyInvoke.invoke(factory);
            if (instance instanceof AppListener)
                doGetListener((AppListener) instance);
            Method[] methods = factory.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (cmd == null)
                    continue;
                doResolveCmd(method, cmd, instance);
            }
        }

        // 发布应用启动事件
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

    // 解析带有 @Cmd 注解的方法
    private static void doResolveCmd(Method method, Cmd cmdAnno, Object o) {
        // 生成一个包装执行器类对象，这个类提供了一些便捷的方法
        ActuatorImpl actuator = new ActuatorImpl(method, cmdAnno, o);
        // 假如这个执行器某些规范不通过，则不进行装配
        if (!actuator.checkMethod())
            return;
        // 根据 type ，执行不同的装配方式
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

    // 对有顺序要求的集合进行排序
    private static void sortActuatorLists() {
        initActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        preActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
        destroyActuators.sort(Comparator.comparingInt(ActuatorImpl::getOrder));
    }

    // 假如 enable() 返回true，则装配至事件发布器
    private static void doGetListener(AppListener listenerObj) {
        if (listenerObj.enable())
            EventPublisher.loadListener(listenerObj);
    }

    /**
     * 假如 @Cmd 注解的 type 属性为 Parser 则装配至转换工厂
     * 检查 parser 方法的方法参数和返回值是否符合要求
     * 即
     *      方法参数只有一个，String 类型
     *      范围值不为空
     *      返回值和 @Cmd 注解的target()属性可以进行相互转换(不强制要求，如果不能则有可能在运行时抛出异常)
     * @param method -
     * @param cmdAnno -
     * @param o -
     */
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
        TransformFactory.addParser(parserFunc, types);
    }

    // 打印欢迎信息，随便写的 ...
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
    }

    /**
     * 对 Actuator 进行包装
     * 对于同包下的类提供一些便捷方法
     * @author flutterdash@qq.com
     * @since 2020/12/29 11:00
     */
    public static class ActuatorImpl implements Actuator {
        // 元信息，方法对象，注解，方法所在的类的实例
        private final Method method;
        private final Cmd cmd;
        private final Object obj;

        // 返回值类型，方法名
        private final Class<?> rtnType;
        private final String cmdName;

        // construct
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
                // 假如是销毁、前置、初始化方法，可以直接执行
                case Destroy:
                case Pre:
                case Init: {
                    return invoke0(items);
                }
                // 普通方法，需要由前置方法执行完成后才能执行(前置方法不抛异常且不返回false)
                default: {
                    InvokeInfo info = doInvokePreProcess();
                    if (info.isSuccess()) {
                        return invoke0(items);
                    } else {
                        if (config.isPrintStackTraceOnException())
                            info.getException().printStackTrace();
                        else
                            cPrint.println(info.getExMsg());
                        return InvokeInfo.simpleSuccess();
                    }
                }
            }
        }

        /**
         * 初始化方法、销毁方法、前置方法，不能有参数
         * 前置方法返回值必须是bool类型
         * @return -
         */
        protected boolean checkMethod() {
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

        // 执行过滤链
        protected InvokeInfo doInvokePreProcess() {
            for (ActuatorImpl actuator : preActuators) {
                // 方法执行结果
                InvokeInfo info = actuator.invoke0(null);
                // 方法执行错误
                if (!info.isSuccess()) {
                    return info;
                } else {
                    boolean result = (boolean) info.get();
                    if (!result)
                        return InvokeInfo.failed(rtnType, null,
                                new RuntimeException("错误信息: " + actuator.cmd.onError()));
                }
            }
            return InvokeInfo.simpleSuccess();
        }

        /**
         * *字符串命令调用的核心实现入口*
         * @param items 执行命令时使用的参数，以按照空格分割成列表
         * @return 执行结果信息
         */
        protected InvokeInfo invoke0(List<String> items) {
            // 在方法执行之前先获取此方法的一些信息
            InvokeInfo info = InvokeInfo.beforeInvoke(cmdName, rtnType, items);
            // 发布命令解析事件
            EventPublisher.onResolveInput(cmdName, items);
            // 由解析工厂将字符串命令解析成Object数组供method对象调用，结果被wrapper包装
            ResultWrapper wrapper = ResolveFactory.transform(method, items);
            // 如果解析成功
            if (wrapper.success) {
                method.setAccessible(true);
                Object rtnVal = null;
                try {
                    // 解析后的参数进行执行
                    rtnVal = method.invoke(obj, wrapper.args);
                    // 得到结果填充给info对象
                    info.finishInvoke(rtnVal, wrapper.args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    // 执行方法时方法内部发生错误，或者参数不匹配，错误信息填充给info对象
                    info.onException(e, wrapper.args);
                }
                // 发布输入解析完成事件
                EventPublisher.onInputResolved(cmdName, rtnVal);
            }
            // 解析失败
            else {
                // 仍然发布输入解析完成事件，此时返回值无效
                EventPublisher.onInputResolved(cmdName,null);
                // 将错误信息填充至info
                info.onException(wrapper.ex, null);
            }
            // 执行信息
            return info;
        }

        /**
         * 使用传入参数的方式执行
         * @param args 调用方法用的参数
         * @return 调用信息
         */
        protected InvokeInfo invokeByArgs(Object ... args) {
            method.setAccessible(true);
            InvokeInfo info = InvokeInfo.beforeInvoke(cmdName, rtnType, null);
            try {
                Object rtnVal = method.invoke(obj, args);
                info.finishInvoke(rtnVal, args);
                return info;
            } catch (Exception e) {
                info.onException(e, args);
                return info;
            }
        }

        // 获取执行方法的优先级
        public int getOrder() {
            return cmd.order();
        }

        // 打印此 方法/命令 的帮助信息， 即将被弃用
        @Deprecated
        public void printInfo() {

        }

    }

}
