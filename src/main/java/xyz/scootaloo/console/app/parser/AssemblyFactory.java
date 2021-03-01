package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.CmdType;
import xyz.scootaloo.console.app.common.*;
import xyz.scootaloo.console.app.config.Author;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.error.CommandInvokeException;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.event.AppListener;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.preset.PresetFactoryManager;
import xyz.scootaloo.console.app.parser.preset.SystemPresetCmd;
import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.InvokeProxy;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 框架核心装配工厂
 * <p>框架中所有工厂的处理入口 {@link  #init(ConsoleConfig)}</p>
 * <p>负责对所有工厂进行装配，并管理一些工厂资源</p>
 * @author flutterdash@qq.com
 * @since 2020/12/28 10:05
 */
public final class AssemblyFactory {
    /** resources */
    private static final Colorful color = ResourceManager.getColorful();
    private static final Console console = ResourceManager.getConsole();
    private static ConsoleBanner bannerPrinter = AssemblyFactory::welcome;
    protected static final Map<String, Actuator> strategyMap = new HashMap<>();
    protected static final List<MethodActuator> initActuators = new ArrayList<>();
    protected static final List<MethodActuator> destroyActuators = new ArrayList<>();
    protected static Map<String, ParameterParser> parserMap = new HashMap<>();

    // 所有可调用的命令
    private static final List<MethodActuator> ALL_COMMANDS = new ArrayList<>();
    // 帮助信息map
    private static final Map<String, String> HELP_MAP = new HashMap<>();

    // config
    private static ConsoleConfig config;
    protected static boolean hasInit = false;

    // 根据配置进行初始化
    public static void init(ConsoleConfig conf) {
        config = conf;
        doInitStrategyFactories();
    }

    /**
     * 根据一个命令名，返回对应的执行器
     * @param cmdName 命令名，或者方法名
     * @return 执行器对象，当没有找到执行器时，返回一个空的执行器实现，这个执行器调用不会触发事件
     */
    public static Actuator findActuator(String cmdName) {
        cmdName = cmdName.toLowerCase(Locale.ROOT);
        Actuator actuator = strategyMap.get(cmdName);
        if (actuator != null)
            return actuator;
        if (!cmdName.equals(""))
            console.println(color.blue("没有这个命令`" + cmdName + "`"));
        return cmd -> {
            // do nothing ...
            return InvokeInfo.simpleSuccess();
        };
    }

    /**
     * 返回系统中所有可调用的命令
     * @return 执行器列表
     */
    public static List<MethodActuator> getAllCommands() {
        return ALL_COMMANDS;
    }

    /**
     * 返回可调用的系统命令集
     * @return 命令名称集合
     */
    public static Set<String> getSysCommands() {
        return getAllCommands().stream()
                .filter(methodActuator -> methodActuator.getCmd().tag().equals(SystemPresetCmd.SYS_TAG))
                .flatMap(methodActuator -> Stream.of(methodActuator.getCmdName(),
                        methodActuator.cmd.name().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
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
        if (config == null) {
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
                doGetPrinterFactory((CPrinterSupplier) factoryInstance);
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
                    doGetListener((AppListener) factoryInstance);
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
        for (Object helpDoc : helpFactories) {
            doGetHelpFactory(helpDoc);
        }

        // 发布应用启动事件
        EventPublisher.onAppStarted(config);
        sortActuatorLists();

        // 执行初始化方法，遇到异常则退出应用
        try {
            for (MethodActuator actuator : initActuators) {
                actuator.invokeCore(null);
            }
        } catch (Exception e) {
            console.onException(config, e, "初始化失败, msg: " + e.getMessage() + "\n", true);
        }

        // 将销毁方法注册到系统关闭钩子中
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Actuator actuator : destroyActuators) {
                InvokeProxy.fun(actuator::invoke).call(null);
            }
        }));
    }

    // 解析带有 @Cmd 注解的方法
    private static void doResolveCmd(Method method, Cmd cmdAnno, Object o) {
        // 生成一个包装执行器类对象，这个类提供了一些便捷的方法
        MethodActuator actuator = new MethodActuator(method, cmdAnno, o);
        // 根据 type ，执行不同的装配方式
        switch (cmdAnno.type()) {
            case Cmd: {
                // 假如这个执行器某些规范不通过，则不进行装配
                if (!actuator.checkMethod())
                    return;
                // 处理解析模式
                ParameterParser parser = parserMap.get(cmdAnno.parser());
                if (parser != null)
                    if (!actuator.setParser(parser))
                        return;
                strategyMap.put(actuator.cmdName, actuator);
                ALL_COMMANDS.add(actuator);
                Cmd cmd = method.getAnnotation(Cmd.class);
                if (!cmd.name().equals("")) {
                    strategyMap.put(cmd.name().toLowerCase(Locale.ROOT), actuator);
                }
            } break;
            case Filter: {
                FilterMethodWrapper.addFilter(actuator);
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
        initActuators.sort(Comparator.comparingInt(MethodActuator::getOrder));
        destroyActuators.sort(Comparator.comparingInt(MethodActuator::getOrder));
        FilterMethodWrapper.sort();
    }

    // 假如 enable() 返回true，则装配至事件发布器
    private static void doGetListener(AppListener listenerObj) {
        if (listenerObj.enable())
            EventPublisher.regListener(listenerObj);
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
    private static void doGetPrinterFactory(CPrinterSupplier printerFactory) {
        ResourceManager.setPrinterFactory(printerFactory);
        DefaultConsole.setPrinter(printerFactory.get());
    }

    // 装配Help工厂
    private static void doGetHelpFactory(Object factory) {
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

            method.setAccessible(true);
            try {
                String helpInfo = (String) method.invoke(factory);
                // 已经执行成功得到了返回值，现在将结果保存到对于的位置
                Actuator actuator = findActuator(methodName);
                if (actuator instanceof MethodActuator) {
                    MethodActuator methodActuator = (MethodActuator) actuator;
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

    /**
     * 对 Actuator 进行包装 <br>
     * 对于同包下的类提供一些便捷方法
     * @author flutterdash@qq.com
     * @since 2020/12/29 11:00
     */
    public static class MethodActuator implements Actuator {
        // 元信息，方法对象，注解，方法所在的类的实例，解析器，以及已经从方法中提取出来的一些信息
        private final Method method;
        private final Cmd cmd;
        private final Object obj;
        private ParameterParser parser;
        private final MethodMeta methodMeta;

        // 返回值类型，方法名
        private final Class<?> rtnType;
        private final String cmdName;

        // construct
        public MethodActuator(Method m, Cmd c, Object o) {
            this.cmd = c;
            this.method = m;
            this.obj = o;
            this.parser = DftParameterParser::transform;

            this.cmdName = method.getName().toLowerCase(Locale.ROOT);
            this.rtnType = method.getReturnType();
            this.methodMeta = MethodMeta.getInstance(method, obj);
        }

        @Override
        public InvokeInfo invoke(List<String> cmdArgs) {
            switch (cmd.type()) {
                // 假如是销毁、过滤器、初始化方法，可以直接执行
                case Destroy:
                case Init: {
                    return invokeCore(cmdArgs);
                }
                // 普通命令方法，需要执行完过滤器后才能执行(过滤器不抛异常且不返回false)
                default: {
                    FilterMessage filterMessage = FilterMethodWrapper.doFilterChain();
                    if (!filterMessage.success) {
                        CommandInvokeException commandInvokeException;
                        if (filterMessage.hasException)
                            commandInvokeException = new CommandInvokeException(filterMessage.errorMsg,
                                    filterMessage.exception);
                        else
                            commandInvokeException = new CommandInvokeException(filterMessage.errorMsg);
                        return InvokeInfo.failed(rtnType, cmdArgs, commandInvokeException
                                .appendExData(methodMeta, obj, cmdArgs, parser.getClass()));
                    } else {
                        return invokeCore(cmdArgs);
                    }
                }
            }
        }

        /**
         * 初始化方法、销毁方法、前置方法，不能有参数
         * 前置方法返回值必须是bool类型
         * Parser解析器已经交由doGetParser处理了
         * @see #doGetParser(Method, Cmd, Object)
         * @return -
         */
        protected boolean checkMethod() {
            CmdType type = cmd.type();
            switch (type) {
                case Destroy:
                case Init: {
                    return checkNormalMethod();
                }
                case Cmd: {
                    if (!parser.check(methodMeta)) {
                        throw new RuntimeException("规范检查不通过");
                    }
                }
            }
            return true;
        }

        private boolean checkNormalMethod() {
            return method.getParameterCount() != 0;
        }

        /**
         * *字符串命令调用的核心实现入口*
         * @param cmdArgs 执行命令时使用的参数，以按照空格分割成列表
         * @return 执行结果信息
         */
        protected InvokeInfo invokeCore(List<String> cmdArgs) {
            // 在方法执行之前先获取此方法的一些信息
            InvokeInfo info = InvokeInfo.beforeInvoke(cmdName, rtnType, cmdArgs);
            // 发布命令解析前事件
            EventPublisher.onResolveInput(cmdName, cmdArgs);
            // 由解析工厂将字符串命令解析成Object数组供method对象调用，结果由wrapper包装
            ResultWrapper wrapper;
            try {
                wrapper = parser.parse(methodMeta, cmdArgs);
            } catch (Exception paramResolveEx) {
                // 这里一般是参数解析异常
                return info.onException(new ParameterResolveException("不能将命令行参数映射到方法参数", paramResolveEx)
                        .appendExData(methodMeta, obj, cmdArgs, parser.getClass()), null);
            }
            // 如果解析成功
            if (wrapper.isSuccess()) {
                try {
                    method.setAccessible(true);
                    // 用解析后的参数对method进行调用
                    Object rtnVal = method.invoke(obj, wrapper.getArgs());
                    // 得到结果填充给info对象
                    info.finishInvoke(rtnVal, wrapper.getArgs());
                } catch (Exception e) {
                    // 执行方法时方法内部发生错误，或者参数不匹配，错误信息填充给info对象
                    info.onException(new CommandInvokeException("方法调用异常:" + e.getMessage(), e)
                            .appendExData(methodMeta, obj, cmdArgs, parser.getClass())
                            , wrapper.getArgs());
                }
                // 记录最近一次的执行信息
                Interpreter.lastInvokeInfo = info;
            }
            // 解析失败: 这里一般是命令行中缺省了必要参数，或者命令行不完整等
            else {
                // 将错误信息填充至info
                info.onException(wrapper.getEx(), null);
            }
            // 发布输入解析完成事件
            EventPublisher.onInputResolved(cmdName, info);
            // 返回调用信息
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
                info.onException(new CommandInvokeException("方法调用异常", e)
                        .appendExData(methodMeta, obj, null, null), args);
                return info;
            }
        }

        // 设置解析器
        public boolean setParser(ParameterParser parser) {
            if (parser != null) {
                this.parser = parser;
                return parser.check(this.methodMeta);
            }
            return true;
        }

        // getter

        // 获取此方法对应的类的实例
        public Object getInstance() {
            return this.obj;
        }

        // 获取方法对象
        public Method getMethod() {
            return this.method;
        }

        // 获取方法的名称
        public String getCmdName() {
            return this.cmdName;
        }

        // 获取此命令方法上的注解
        public Cmd getCmd() {
            return this.cmd;
        }

        // 获取执行方法的优先级
        public int getOrder() {
            return cmd.order();
        }

        // 打印此 方法/命令 的帮助信息
        public String getHelpInfo() {
            String body = HELP_MAP.get(this.cmdName);
            if (body == null) {
                return "没有此命令的帮助信息";
            } else {
                String title = "\n[" + cmdName;
                title += !cmd.name().equals("") ? ", " + cmd.name() : "";
                title += "]";
                return title + body;
            }
        }

    }

    /**
     * 过滤器方法专用包装类，主要负责对过滤器方法规范进行检查，执行过滤器方式，以及管理过滤器相关的操作。
     * @author flutterdash@qq.com
     * @since 2021/3/1 11:00
     */
    private static class FilterMethodWrapper {
        /** 全局过滤器 */
        private static final List<FilterMethodWrapper> filters = new ArrayList<>();

        /** resources */
        private static final FilterMessage filterMessage = new FilterMessage();

        /** instance properties */
        private final MethodMeta meta;  // 方法信息
        private final String errorMsg;  // 对应 Cmd 注解上的 error
        private final int order;        // 优先级

        /**
         * 按照过滤器的 order 值进行排序
         */
        public static void sort() {
            if (filters.isEmpty())
                return;
            filters.sort(Comparator.comparingInt(FilterMethodWrapper::getOrder));
        }

        /**
         * 主要检查此方法的返回值，返回值必须是bool类型
         * @param method 一个过滤器的 method 对象
         * @return 是否符合规范. true 符合; false 不符合.
         */
        private static boolean checkFilterMethod(Method method) {
            Class<?> rtnType = method.getReturnType();
            return rtnType == boolean.class || rtnType == Boolean.class;
        }

        /**
         * 执行过滤链，这个方法会按照顺序调用所有过滤器。<br>
         * 当其中某个过滤器返回了 false, 或者抛出了异常，则中断过滤操作。
         * @return 过滤器执行信息
         */
        public static FilterMessage doFilterChain() {
            if (filters.isEmpty())
                return filterMessage.ok();
            filterMessage.clear();
            for (FilterMethodWrapper filter : filters) {
                boolean pass = InvokeProxy.fun(filter::invoke)
                        .addHandle(filterMessage::onException).setDefault(false).call();
                if (!pass) { // 过滤器方法返回 false; 不放行
                    return filterMessage.onCutOff(filter.getErrorMsg());
                }
            }
            return filterMessage.ok();
        }

        /**
         * 增加过滤器，加入这个过滤器之前，会先检查它的方法格式是否符合要求，如果不符合，则不会被添加到过滤链
         * @param methodActuator 方法执行器，根据这个执行器生成过滤器对象
         */
        public static void addFilter(MethodActuator methodActuator) {
            if (!checkFilterMethod(methodActuator.method)) {
                console.err("过滤器方法格式错误:\n" + getSpecification());
                return;
            }
            filters.add(new FilterMethodWrapper(methodActuator));
        }

        // *constructor*
        private FilterMethodWrapper(MethodActuator methodActuator) {
            Cmd cmdAnno = methodActuator.cmd;
            this.errorMsg = cmdAnno.onError().isEmpty() ?
                    "被 " + ClassUtils.getMethodDescribe(methodActuator.method) + " 拦截" : cmdAnno.onError();
            this.order = cmdAnno.order();
            this.meta = methodActuator.methodMeta;
        }

        // 调用过滤器方法
        private boolean invoke() throws InvocationTargetException, IllegalAccessException {
            // 获取参数
            Class<?>[] paramTypes = meta.parameterTypes;
            List<Object> methodParamList = new ArrayList<>();
            for (Class<?> curParamType : paramTypes) {
                // 根据类型注入值
                if (curParamType == String.class)
                    methodParamList.add(Interpreter.getCallingCommand());
                else
                    TransformFactory.getDefVal(curParamType);
            }

            this.meta.method.setAccessible(true);
            return (boolean) meta.method.invoke(meta.obj, methodParamList.toArray());
        }

        // 获取过滤器的优先级信息
        private int getOrder() {
            return order;
        }

        // 此过滤器的错误时信息
        private String getErrorMsg() {
            return errorMsg;
        }

        // 过滤器方法的规范，当有过滤器不符合要求时输出这些内容
        private static String getSpecification() {
            return "1. 必须是实例方法.\n" +
                    "2. 方法上必须有`@Cmd`注解.\n" +
                    "3. @Cmd 的 type 属性必须是 Filter.\n" +
                    "4. 方法返回值必须是布尔值.\n" +
                    "5. 方法参数目前仅支持 String 类型，用于接受当前输入的命令\n";
        }

    }

    // pojo
    private static class FilterMessage {

        private boolean success;        // 是否成功
        private String errorMsg;        // 错误信息
        private boolean hasException;   // 是否有异常
        private Exception exception;    // 异常

        public void clear() {
            this.success = false;
            this.errorMsg = null;
            this.exception = null;
            this.hasException = false;
        }

        public void onException(Exception ex) {
            this.success = false;
            this.exception = ex;
            this.hasException = true;
        }

        public FilterMessage onCutOff(String msg) {
            this.success = false;
            this.errorMsg = msg;
            return this;
        }

        public FilterMessage ok() {
            this.success = true;
            return this;
        }

    }

}
