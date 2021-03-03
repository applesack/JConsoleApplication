package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.CmdType;
import xyz.scootaloo.console.app.anno.mark.Public;
import xyz.scootaloo.console.app.common.Colorful;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.error.CommandInvokeException;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.preset.SystemPresetCmd;
import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.InvokeProxy;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 解释器
 * @author flutterdash@qq.com
 * @since 2021/1/6 23:00
 */
@Public
public final class Interpreter {
    private final ConsoleConfig config;
    protected static InvokeInfo lastInvokeInfo;
    protected static String CALLING_COMMAND;

    private final static Map<String, MethodActuator> strategyMap = new HashMap<>();
    private final static Console console = ResourceManager.getConsole();
    private final static Colorful color = ResourceManager.getColorful();

    /**
     * 获取解释器的引用，请从 {@link xyz.scootaloo.console.app.ApplicationRunner#getInterpreter(ConsoleConfig)} 处获取
     * @param config 配置
     */
    public Interpreter(ConsoleConfig config) {
        if (!AssemblyFactory.hasInit) {
            AssemblyFactory.init(config, this);
            AssemblyFactory.hasInit = true;
        }
        this.config = config;
    }

    /**
     * 解释执行一段命令行
     * @param cmd 命令行
     * @return 方法调用信息
     */
    public InvokeInfo interpret(String cmd) {
        List<String> allTheCmdItem = StringUtils.toList(cmd);
        String cmdName = getCmdName(allTheCmdItem);
        Optional<MethodActuator> actuatorWrapper = findActuatorByName(cmdName);
        if (actuatorWrapper.isPresent()) {
            InvokeInfo filterChainInfo = doFilterChain(actuatorWrapper.get(), allTheCmdItem);
            if (!filterChainInfo.isSuccess())
                return filterChainInfo;
        } else {
            return lackCommandException();
        }
        return actuatorWrapper.get().invoke(allTheCmdItem);
    }

    /**
     * 根据方法名调用无参方法
     * @param name 框架容器中管理的无参方法的方法名
     * @return 调用信息
     */
    public InvokeInfo call(String name) {
        Optional<MethodActuator> actuatorWrapper = findActuatorByName(name);
        if (actuatorWrapper.isPresent()) {
            InvokeInfo filterChainInfo = doFilterChain(actuatorWrapper.get(), null);
            if (!filterChainInfo.isSuccess())
                return filterChainInfo;
        } else {
            return lackCommandException();
        }
        return actuatorWrapper.get().invokeByArgs();
    }

    /**
     * 根据方法名，方法参数来调用方法<br>
     * 这个方法必须是容器管理的，否则无法调用
     * @param name 方法名
     * @param args 此方法需要的参数
     * @return 调用信息
     */
    public InvokeInfo call(String name, Object ... args) {
        Optional<MethodActuator> actuatorWrapper = findActuatorByName(name);
        if (actuatorWrapper.isPresent()) {
            InvokeInfo filterChainInfo = doFilterChain(actuatorWrapper.get(), null);
            if (!filterChainInfo.isSuccess())
                return filterChainInfo;
        } else {
            return lackCommandException();
        }
        return actuatorWrapper.get().invokeByArgs(args);
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

    /**
     * 执行过滤链
     * @param actuator 方法执行器
     * @param cmdArgs 方法参数
     * @return 执行结果
     */
    protected InvokeInfo doFilterChain(MethodActuator actuator, List<String> cmdArgs) {
        FilterMessage filterMessage = FilterMethodWrapper.doFilterChain();
        if (!filterMessage.success) {
            CommandInvokeException commandInvokeException;
            if (filterMessage.hasException) {
                commandInvokeException = new CommandInvokeException(filterMessage.errorMsg,
                        filterMessage.exception);
                commandInvokeException.setErrorInfo(ErrorCode.FILTER_ON_EXCEPTION);
            } else {
                commandInvokeException = new CommandInvokeException(filterMessage.errorMsg);
                commandInvokeException.setErrorInfo(ErrorCode.FILTER_INTERCEPT);
            }
            return InvokeInfo.failed(actuator.rtnType, cmdArgs, commandInvokeException
                    .appendExData(actuator.methodMeta, actuator.obj, cmdArgs, actuator.parser.getClass()));
        } else {
            return InvokeInfo.simpleSuccess();
        }
    }

    public void setCurrentCallingCommand(String cmd) {
        CALLING_COMMAND = cmd;
    }

    /**
     * 返回可调用的系统命令集
     * @return 命令名称集合
     */
    public static Set<String> getSysCommands() {
        return strategyMap.values().stream()
                .filter(methodActuator -> methodActuator.getCmd().tag().equals(SystemPresetCmd.SYS_TAG))
                .flatMap(methodActuator -> Stream.of(methodActuator.getCmdName(),
                        methodActuator.cmd.name().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }

    protected InvokeInfo lackCommandException() {
        return InvokeInfo.failed(null, null,
                new CommandInvokeException("没有这个命令").setErrorInfo(ErrorCode.LACK_COMMAND_HANDLER));
    }

    protected static String getCallingCommand() {
        return CALLING_COMMAND;
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

    /**
     * 根据一个命令名，返回对应的执行器
     * @param name 命令名，或者方法名
     * @return 执行器对象，当没有找到执行器时，返回一个空的执行器实现，这个执行器调用不会触发事件
     */
    protected Optional<MethodActuator> findActuatorByName(String name) {
        name = name.toLowerCase(Locale.ROOT);
        MethodActuator actuator = strategyMap.get(name);
        if (actuator != null)
            return Optional.of(actuator);
        if (name.isEmpty())
            console.println(color.blue("没有这个命令`" + name + "`"));
        return Optional.empty();
    }

    protected void loadCommandMethod(MethodActuator actuator) {
        strategyMap.put(actuator.cmdName, actuator);
        Cmd cmdAnno = actuator.cmd;
        if (!cmdAnno.name().equals("")) {
            strategyMap.put(cmdAnno.name().toLowerCase(Locale.ROOT), actuator);
        }
    }

    protected void loadFilter(MethodActuator actuator) {
        FilterMethodWrapper.addFilter(actuator);
    }

    protected void sortFilter() {
        FilterMethodWrapper.sort();
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
            return invokeCore(cmdArgs);
        }

        /**
         * 初始化方法、销毁方法、前置方法，不能有参数
         * 前置方法返回值必须是bool类型
         * Parser解析器已经交由doGetParser处理了
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
                        .appendExData(methodMeta, obj, cmdArgs, parser.getClass())
                        .setErrorInfo(ErrorCode.PARAMETER_PARSER_ERROR), null);
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
                            .setErrorInfo(ErrorCode.METHOD_INVOKE_ERROR), wrapper.getArgs());
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
                        .appendExData(methodMeta, obj, null, null)
                        .setErrorInfo(ErrorCode.METHOD_INVOKE_ERROR), args);
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
            Optional<String> body = AssemblyFactory.getHelp(this.cmdName);
            if (!body.isPresent()) {
                return "没有此命令的帮助信息";
            } else {
                String title = "\n[" + cmdName;
                title += !cmd.name().equals("") ? ", " + cmd.name() : "";
                title += "]";
                return title + body.get();
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
