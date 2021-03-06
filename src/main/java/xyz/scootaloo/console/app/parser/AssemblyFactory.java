package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.application.processor.CallBack;
import xyz.scootaloo.console.app.common.*;
import xyz.scootaloo.console.app.config.Author;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.event.AppListener;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.Interpreter.MethodActuator;
import xyz.scootaloo.console.app.parser.preset.PresetFactoryManager;
import xyz.scootaloo.console.app.util.FunctionDesc;
import xyz.scootaloo.console.app.util.InvokeProxy;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

/**
 * 框架核心装配工厂
 * <p>框架中所有工厂的处理入口 {@link  #init(ConsoleConfig, Interpreter)}</p>
 * <pre>
 * 负责对所有工厂进行装配，并管理一些工厂资源
 * 管理的资源有:
 *      初始化方法列表;
 *      销毁方法列表;
 *      参数解析器集合;
 *      命令帮助信息集合;
 * </pre>
 * @author flutterdash@qq.com
 * @since 2020/12/28 10:05
 */
public final class AssemblyFactory {
    /** resources */
    private static final Console console = ResourceManager.getConsole();
    private static ConsoleBanner bannerPrinter = AssemblyFactory::welcome;

    protected static final List<CallBack>     initActuators = new ArrayList<>();
    protected static final List<CallBack>  destroyActuators = new ArrayList<>();
    protected static Map<String, ParameterParser> parserMap = new HashMap<>();
    private final static Map<String, String>       HELP_MAP = new HashMap<>();

    // 所有可调用的命令
    private static final List<MethodActuator> ALL_COMMANDS = new LinkedList<>();
    // 与装配工厂绑定的解释器
    private static Interpreter interpreter;

    // 配置
    private static ConsoleConfig config;
    protected static volatile boolean hasInit = false;

    /**
     * 根据配置进行初始化
     * @param conf 控制台配置
     * @param interpreter_ 解释器
     */
    protected static void init(ConsoleConfig conf, Interpreter interpreter_) {
        config = conf;
        interpreter = interpreter_;
        doInitStrategyFactories();
    }

    /**
     * 返回系统中所有可调用的命令
     * @return 执行器列表
     */
    public static List<MethodActuator> getAllCommands() {
        return ALL_COMMANDS;
    }

    /**
     * 根据名称查找指定的执行器
     * @param name 执行器的名称
     * @return 执行器
     */
    public static Actuator findActuator(String name) {
        if (interpreter == null)
            throw new RuntimeException("启动方式错误");
        Optional<MethodActuator> methodActuator = interpreter.findActuatorByName(name);
        if (methodActuator.isPresent())
            return methodActuator.get();
        return (cmdArgs) -> InvokeInfo.simpleSuccess();
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
    private static void doInitStrategyFactories() {
        if (hasInit) // 假如已经初始化过，则退出
            return;
        if (config == null || interpreter == null) {
            console.exit0("未加载到配置");
            return;
        } else {
            hasInit = true;
        }

        // 将预置的工厂注入进来
        Set<Supplier<Object>> factories = PresetFactoryManager.getFactories(config);

        // 帮助工厂
        List<HelpDoc> helpFactories = new ArrayList<>();

        // 预处理: 优先装配参数解析器
        Set<Object> retainSet = new LinkedHashSet<>();
        for (Supplier<Object> factory : factories) {
            Object factoryInstance = factory.get();
            if (factoryInstance == null)
                continue;
            // 全局输出实现工厂方法
            if (factoryInstance instanceof CPrinterSupplier)
                loadPrinterFactory((CPrinterSupplier) factoryInstance);
            // banner printer
            if (factoryInstance instanceof ConsoleBanner)
                bannerPrinter = (ConsoleBanner) factoryInstance;
            // 装配参数解析器
            if (factoryInstance instanceof NameableParameterParser) {
                NameableParameterParser parser = (NameableParameterParser) factoryInstance;
                parserMap.put(parser.name(), parser);
                continue;
            }
            // 装配操作处理器
            if (factoryInstance instanceof OptionHandler) {
                ExtraOptionHandler.addExtraOption((OptionHandler) factoryInstance);
                continue;
            }
            // 收集帮助文档
            if (factoryInstance instanceof HelpDoc) {
                helpFactories.add((HelpDoc) factoryInstance);
            } else {
                retainSet.add(factoryInstance);
                if (factoryInstance instanceof AppListener)
                    loadListener((AppListener) factoryInstance);
            }
        }

        // 输出欢迎信息
        bannerPrinter.printBanner();

        // 处理命令工厂
        for (Object factoryInstance : retainSet) {
            Class<?> factoryClass = factoryInstance.getClass();
            Method[] methods = factoryClass.getDeclaredMethods();
            for (Method method : methods) {
                method.setAccessible(true);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (cmd == null)
                    continue;
                doResolveCmd(method, cmd, factoryInstance);
            }
        }

        // 处理帮助工厂
        for (HelpDoc helpDoc : helpFactories) {
            loadHelpFactory(helpDoc);
        }

        // 发布应用启动事件
        EventPublisher.onAppStarted(config);
        sortResources();

        // 执行初始化方法，遇到异常则退出应用
        try {
            initActuators.forEach(CallBack::call);
        } catch (Exception e) {
            console.onException(config, e, "初始化失败, msg: " + e.getMessage() + "\n", true);
        }

        // 将销毁方法注册到系统关闭钩子中
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                initActuators.forEach(CallBack::call)));
    }

    /**
     * 解析带有 @Cmd 注解的方法
     * <p>在这里会按照 @Cmd 注解的类型type进行装配</p>
     * @param method 一个标记有 @Cmd 注解的实例方法
     * @param cmdAnno 注解对象
     * @param o method对象所属的实例
     */
    private static void doResolveCmd(Method method, Cmd cmdAnno, Object o) {
        // 生成一个包装执行器类对象，这个类提供了一些便捷的方法
        MethodActuator actuator = new MethodActuator(method, cmdAnno, o);
        // 根据 type ，执行不同的装配方式
        switch (cmdAnno.type()) {
            case Cmd: {
                // 处理解析模式
                ParameterParser parser = parserMap.get(cmdAnno.parser());
                if (parser != null)
                    if (!actuator.setParser(parser))
                        return;
                ALL_COMMANDS.add(actuator);
                interpreter.loadCommandMethod(actuator);
            } break;
            case Filter: {
                interpreter.loadFilter(actuator);
            } break;
            case Init: {
                SimpleCallableMethod.checkAndAdd(method, o, cmdAnno, initActuators);
            } break;
            case Destroy: {
                SimpleCallableMethod.checkAndAdd(method, o, cmdAnno, destroyActuators);
            } break;
            case Parser: {
                loadParser(method, cmdAnno, o);
            }
        }
    }

    /**
     * 对有顺序要求的资源进行排序
     */
    private static void sortResources() {
        initActuators.sort(Comparator.comparingInt(CallBack::getOrder));
        destroyActuators.sort(Comparator.comparingInt(CallBack::getOrder));
        interpreter.sortFilter();
    }

    /**
     * 装配事件监听器
     * @param listenerObj 假如 enable() 返回true，则装配至事件发布器
     */
    private static void loadListener(AppListener listenerObj) {
        if (listenerObj.enable())
            EventPublisher.regListener(listenerObj);
    }

    /**
     * <pre>假如 @Cmd 注解的 type 属性为 Parser 则装配至转换工厂
     * 检查 parser 方法的方法参数和返回值是否符合要求
     * 即
     *      方法参数只有一个，String 类型
     *      范围值不为空
     *      返回值和 @Cmd 注解的target()属性可以进行相互转换(不强制要求，如果不能则有可能在运行时抛出异常) </pre>
     * @param method -
     * @param cmdAnno -
     * @param o -
     */
    private static void loadParser(Method method, Cmd cmdAnno, Object o) {
        if (cmdAnno.targets().length == 0)
            return;
        Class<?>[] types = cmdAnno.targets();
        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || params[0] != String.class)
            return;
        if (method.getReturnType() == void.class || method.getReturnType() == Void.class)
            return;
        TransformFactory.addParser((str) -> {
            try {
                method.setAccessible(true);
                return method.invoke(o, str);
            } catch (IllegalAccessException | InvocationTargetException e) {
                console.onException(config, e);
                return null;
            }
        }, types);
    }

    /**
     * 指定其他的输出方式，默认输出方法是 System.out.print
     * @see xyz.scootaloo.console.app.common.DefaultConsole 默认实现
     * @param printerFactory 自定义实现工厂方法实现
     */
    private static void loadPrinterFactory(CPrinterSupplier printerFactory) {
        ResourceManager.setPrinterFactory(printerFactory);
        DefaultConsole.setPrinter(printerFactory.get());
    }

    /**
     * 加载Help工厂
     * <p>这个方法将会遍历实现了 HelpDoc 接口的类的所有方法，找出它的所有实例方法，
     * 将其中无参且返回值是字符串类型的方法依次调用，获取返回值做为帮助文档信息，按照该实例方法的名称装配到指定的命令行处理器上。<br>
     * 你可以使用 {@code help xx} 命令来查看被注册进来的帮助文档信息。</p>
     *
     * <p>为了将帮助文档方法和 Cmd 方法区分开来，可以在方法名中插入下划线，这个用法在以下示例</p>
     * @see xyz.scootaloo.console.app.parser.preset.SystemPresetCmd.Help 框架中使用 HelpDoc 的示例
     * @param factory 包含帮助信息的类
     */
    private static void loadHelpFactory(HelpDoc factory) {
        Class<?> helpObjClass = factory.getClass();
        Method[] methods = helpObjClass.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName().toLowerCase(Locale.ROOT);
            methodName = StringUtils.ignoreChar(methodName, '_');

            // 检查方法的参数和返回值
            if (method.getParameterCount() != 0)
                continue;
            if (method.getReturnType() != String.class)
                continue;

            try {
                method.setAccessible(true);
                String helpInfo = (String) method.invoke(factory);
                // 已经执行成功得到了返回值，现在将结果保存到对于的位置
                Optional<MethodActuator> actuatorWrapper = interpreter.findActuatorByName(methodName);
                if (actuatorWrapper.isPresent()) {
                    MethodActuator methodActuator = actuatorWrapper.get();
                    HELP_MAP.put(methodActuator.getCmdName(), helpInfo);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                console.onException(config, e, "装配帮助信息时遇到异常: " + e.getMessage() + ", 类: "
                        + helpObjClass.getSimpleName() + ", 方法名称: " + method.getName());
            }
        }
    }

    // 打印欢迎信息，随便写的 ...
    private static void welcome() {
        if (!config.isPrintWelcome())
            return;
        Author author = config.getAuthor();
        console.println(":: " + config.getAppName() + " ::");
        greetings().ifPresent(console::println);
        Optional.ofNullable(author.getName()).ifPresent(name ->
                console.println("author: " + name));
        Optional.ofNullable(author.getEmail()).ifPresent(email ->
                console.println("email: " + author.getEmail()));
        Optional.ofNullable(author.getCreateDate()).ifPresent(date ->
                console.println("create since: " + author.getCreateDate()));
        Optional.ofNullable(author.getUpdateDate()).ifPresent(date ->
                console.println("last update: " + author.getUpdateDate()));
        Optional.ofNullable(author.getComment()).ifPresent(console::println);
        console.println("");
    }

    // 根据时间输出问候语
    private static Optional<String> greetings() {
        Properties properties = System.getProperties();
        String username = properties.getProperty("user.name");
        if (username == null)
            return Optional.empty();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String greetings;
        if (hour >= 4 && hour < 11)
            greetings = "Good morning. ";
        else if (hour >= 11 && hour <= 18) {
            greetings = "Good afternoon. ";
        } else {
            greetings = "Good evening. ";
        }
        return Optional.of(greetings + username);
    }

    protected static Optional<String> getHelp(String cmdName) {
        return Optional.ofNullable(HELP_MAP.get(cmdName));
    }

    /**
     * 对无参方法的简单封装，使其可以直接调用
     * @author flutterdash@qq.com
     * @since 2020/3/3 21:45
     */
    private static class SimpleCallableMethod implements CallBack {
        final Method method;
        final Object object;
        final int order;

        private SimpleCallableMethod(Method method, Object o, int order) {
            this.method = method;
            this.object = o;
            this.order = order;
        }

        public static void checkAndAdd(Method method, Object obj, Cmd cmd, Collection<CallBack> collection) {
            if (!checkMethod(method) || collection == null)
                return;
            collection.add(new SimpleCallableMethod(method, obj, cmd.order()));
        }

        // 只有方法无参数时，才返回true
        private static boolean checkMethod(Method method) {
            return method.getParameterCount() == 0;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public void call() {
            method.setAccessible(true);
            InvokeProxy.fun((FunctionDesc.Rtn1P<Object, Object>) method::invoke).call(object);
        }

    }

}
