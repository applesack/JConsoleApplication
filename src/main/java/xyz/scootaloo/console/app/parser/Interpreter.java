package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.mark.Public;
import xyz.scootaloo.console.app.client.Client;
import xyz.scootaloo.console.app.client.ClientCenter;
import xyz.scootaloo.console.app.client.ResourcesHandler;
import xyz.scootaloo.console.app.common.Colorful;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.error.CommandInvokeException;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.preset.SystemPresetCmd;
import xyz.scootaloo.console.app.support.InvokeProxy;
import xyz.scootaloo.console.app.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 解释器
 * <p>整个框架的核心功能实现类，
 * 负责解释执行命令行，提供API调用框架中管理的方法。</p>
 *
 * @author flutterdash@qq.com
 * @since 2021/1/6 23:00
 */
@Public
public final class Interpreter {
    // 单例资源
    private static volatile Interpreter INSTANCE;
    private static final        Console console = ResourceManager.getConsole();
    private static final       Colorful color   = ResourceManager.getColorful();

    // 解释器中维护了一个Map用于管理注册到框架中的方法执行器，也就是标记有 @Cmd 注解的方法
    private static final Map<String, MethodActuator> strategyMap = new HashMap<>();

    private final ConsoleConfig       config;
    private final ClientCenter        clientCenter;
    protected ThreadLocal<InvokeInfo> lastInvokeInfo = new ThreadLocal<>();
    private final ThreadLocal<Client> localUser      = new ThreadLocal<>();

    /**
     * 单例，并初始化了几个工厂(这些工厂在整个框架生命周期中只会被初始化一次)
     * @param config 配置
     */
    private Interpreter(ConsoleConfig config) {
        this.config = config;
        if (!AssemblyFactory.hasInit) {
            AssemblyFactory.init(config, this);
            AssemblyFactory.hasInit = true;
        }
        this.clientCenter = ClientCenter.getInstance(this);
        ExtraOptionHandler.setInterpreter(this);
    }

    // 双重检查的单例
    public static Interpreter getInstance(ConsoleConfig config) {
        if (INSTANCE == null) {
            synchronized (Interpreter.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Interpreter(config);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 获取当前用户的客户端对象，可以通过这个对象操作给当前用户分配的资源
     * @return 客户端对象
     */
    public static Client getCurrentUser() {
        if (INSTANCE == null)
            throw new RuntimeException("解释器未初始化");
        INSTANCE.checkAndSet();
        return INSTANCE.localUser.get();
    }

    /**
     * 返回所有可调用的系统命令集合
     * @return 命令名称集合
     */
    public static Set<String> getSysCommands() {
        if (INSTANCE == null)
            throw new RuntimeException("解释器未初始化");
        return strategyMap.values().stream()
                .filter(methodActuator -> methodActuator.getCmd().tag().equals(SystemPresetCmd.SYS_TAG))
                .flatMap(methodActuator -> Stream.of(methodActuator.getCmdName(),
                        methodActuator.cmd.name().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toSet());
    }

    //----------------------------------------核心功能------------------------------------------

    /**
     * 为当前线程创建一个新的用户
     * <p>假如是多用户环境，为了区别不同的用户，需要为使用解释器功能的用户分配一些独立的资源</p>
     * @param userKey 用户标识，此标识必须是唯一的
     * @return 一个资源处理器回调，这个回调将销毁为此用户分配的资源
     */
    public ResourcesHandler setUser(String userKey) {
        Client user = newUser(userKey);
        return user.shutdown();
    }

    private Client newUser(String userKey) {
        Client user = clientCenter.createUser(userKey);
        this.localUser.set(user);
        return user;
    }

    /**
     * 解释执行一段命令行
     * @param commandline 命令行
     * @return 方法调用信息
     */
    public InvokeInfo interpret(String commandline) {
        checkAndSet();
        // 空命令，不做处理
        if (commandline.trim().isEmpty())
            return InvokeInfo.simpleSuccess();
        // 记录当前执行的命令行
        getCurrentUser().getResources().setCallingCommand(commandline);
        String cmdName = Actuator.getCommandName(commandline); // 当前执行的命令行的命令名
        String cmdArgs = Actuator.getCommandArgs(commandline); // 当前执行的命令行的命令参数
        // 检查是否能使用其他方式处理，假如这里返回true，表示已经处理过，则这里可以直接退出
        if (ExtraOptionHandler.handle(cmdName, cmdArgs))
            return InvokeInfo.simpleSuccess();
        // 找到命令方法，执行，得到结果
        Optional<MethodActuator> actuatorWrapper = findActuatorByName(cmdName);
        if (actuatorWrapper.isPresent()) {
            // 执行命令方法之前，先执行过滤器
            InvokeInfo filterChainInfo = doFilterChain(actuatorWrapper.get(), cmdArgs);
            if (!filterChainInfo.isSuccess())
                // 过滤器未通过，返回出错原因
                return filterChainInfo;
        } else {
            // 没有找到能处理命令行的方法
            return lackCommandException(cmdName);
        }
        // 最终执行
        return actuatorWrapper.get().invoke(cmdArgs);
    }

    /**
     * 根据方法名调用无参方法
     * @param name 框架容器中管理的无参方法的方法名
     * @return 调用信息
     */
    public InvokeInfo call(String name) {
        return call(name, new Object[0]);
    }

    /**
     * 根据方法名，方法参数来调用方法<br>
     * 这个方法必须是容器管理的，否则无法调用
     * @param name 方法名
     * @param args 此方法需要的参数
     * @return 调用信息
     */
    public InvokeInfo call(String name, Object ... args) {
        checkAndSet();
        Optional<MethodActuator> actuatorWrapper = findActuatorByName(name);
        if (actuatorWrapper.isPresent()) {
            InvokeInfo filterChainInfo = doFilterChain(actuatorWrapper.get(), null);
            if (!filterChainInfo.isSuccess())
                return filterChainInfo;
        } else {
            return lackCommandException(name);
        }
        return actuatorWrapper.get().invokeByArgs(args);
    }

    /**
     * 将一个键值对放置到当前用户的变量池中
     * @param key 键
     * @param value 值
     */
    public void set(String key, Object value) {
        checkAndSet();
        getCurrentUser().getResources().getVariablePool().put(key, value);
    }

    //----------------------------------------------------------------------------------------------

    /**
     * @return 获取当前解释器的配置对象
     */
    public ConsoleConfig getConfig() {
        return this.config;
    }

    /**
     * 执行过滤链
     * @param actuator 方法执行器
     * @param cmdArgs 方法参数
     * @return 执行结果
     */
    protected InvokeInfo doFilterChain(MethodActuator actuator, String cmdArgs) {
        // 执行过滤链，得到执行结果
        FilterChainMessage filterChainMessage = FilterMethodWrapper.doFilterChain();
        // 执行不成功
        if (!filterChainMessage.isSuccess()) {
            CommandInvokeException commandInvokeException;
            if (filterChainMessage.hasException()) {
                commandInvokeException = new CommandInvokeException(filterChainMessage.getErrorMsg(),
                        filterChainMessage.getException());
                commandInvokeException.setErrorInfo(ErrorCode.FILTER_ON_EXCEPTION);
            } else {
                commandInvokeException = new CommandInvokeException(filterChainMessage.errorMsg);
                commandInvokeException.setErrorInfo(ErrorCode.FILTER_INTERCEPT);
            }
            // 将过滤链中包含的错误信息打包传递出去
            return InvokeInfo.failed(actuator.rtnType, cmdArgs, commandInvokeException
                    .appendExData(actuator.methodMeta, actuator.obj, cmdArgs, actuator.parser.getClass()));
        }
        // 执行成功
        else {
            return InvokeInfo.simpleSuccess();
        }
    }

    // 返回一个异常描述
    private InvokeInfo lackCommandException(String cmdName) {
        return InvokeInfo.failed(null, null,
                new CommandInvokeException("没有这个命令: `" + cmdName + "`")
                        .setErrorInfo(ErrorCode.LACK_COMMAND_HANDLER));
    }

    /**
     * 当此线程没有设置用户信息时，默认分配到默认用户
     */
    private void checkAndSet() {
        Client user = this.localUser.get();
        if (user == null)
            this.localUser.set(clientCenter.getPublicUser());
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

    // 加载方法执行器到解释器中来
    protected void loadCommandMethod(MethodActuator actuator) {
        strategyMap.put(actuator.cmdName, actuator);
        Cmd cmdAnno = actuator.cmd;
        if (!cmdAnno.name().equals("")) {
            strategyMap.put(cmdAnno.name().toLowerCase(Locale.ROOT), actuator);
        }
    }

    // 执行过滤链
    protected void loadFilter(MethodActuator actuator) {
        FilterMethodWrapper.addFilter(actuator);
    }

    // 排序过滤器
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
        public InvokeInfo invoke(String commandArgs) {
            return invokeCore(commandArgs);
        }

        /**
         * *字符串命令调用的核心实现入口*
         * @param cmdArgs 执行命令时使用的参数，以按照空格分割成列表
         * @return 执行结果信息
         */
        protected InvokeInfo invokeCore(String cmdArgs) {
            // 在方法执行之前先获取此方法的一些信息
            InvokeInfo info = InvokeInfo.beforeInvoke(cmdName, rtnType, cmdArgs);
            // 发布命令解析前事件
            List<String> tmpArgs = Actuator.splitCommandArgsBySpace(cmdArgs);
            EventPublisher.onResolveInput(cmdName, tmpArgs);
            cmdArgs = String.join(" ", tmpArgs);
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
                Interpreter.INSTANCE.lastInvokeInfo.set(info);
            }
            // 解析失败: 这里一般是命令行中缺省了必要参数，或者命令行不完整等
            else {
                // 将错误信息填充至info
                info.onException(wrapper.getEx(), null);
            }
            // 发布命令行解析完成事件
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

        // 打印此 方法/命令 的帮助信息
        public String getHelpInfo() {
            Optional<String> body = AssemblyFactory.getHelp(this.cmdName);
            if (!body.isPresent()) {
                return "没有此命令的帮助信息";
            } else {
                String title = "\n[" + cmdName;
                title += !cmd.name().equals("") ? ", " + cmd.name() : "";
                title += "]\n";
                return title + body.get();
            }
        }

        @Override
        public String toString() {
            return "MethodActuator{" +
                    "method=" + method +
                    ", cmd=" + cmd +
                    ", obj=" + obj +
                    ", parser=" + parser +
                    ", methodMeta=" + methodMeta +
                    ", rtnType=" + rtnType +
                    ", cmdName='" + cmdName + '\'' +
                    '}';
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
        public static FilterChainMessage doFilterChain() {
            FilterChainMessage filterChainMessage = getCurrentUser().getResources().getFilterChainMessage();
            filterChainMessage.reset();
            if (filters.isEmpty())
                return filterChainMessage.ok();
            for (FilterMethodWrapper filter : filters) {
                boolean pass = InvokeProxy.fun(filter::invoke)
                        .addHandle(filterChainMessage::onException).setDefault(false).call();
                if (!pass) { // 过滤器方法返回 false; 不放行
                    return filterChainMessage.onCutOff(filter.getErrorMsg());
                }
            }
            return filterChainMessage.ok();
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
                    methodParamList.add(INSTANCE.localUser.get().getResources().getCallingCommand());
                else
                    TransformFactory.getDefVal(curParamType);
            }

            this.meta.method.setAccessible(true);
            return (boolean) meta.method.invoke(meta.owner, methodParamList.toArray());
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

        @Override
        public String toString() {
            return "FilterMethodWrapper{" +
                    "meta=" + meta +
                    ", errorMsg='" + errorMsg + '\'' +
                    ", order=" + order +
                    '}';
        }

    }

    /**
     * 过滤链执行信息
     */
    public static class FilterChainMessage {
        private boolean      success = false;   // 是否成功
        private String      errorMsg = null;    // 错误信息
        private boolean hasException = false;   // 是否有异常
        private Exception  exception = null;    // 异常

        private void reset() {
            this.success = false;
            this.errorMsg = null;
            this.hasException = false;
            this.exception = null;
        }

        public void onException(Exception ex) {
            this.success = false;
            this.exception = ex;
            this.hasException = true;
        }

        public FilterChainMessage onCutOff(String msg) {
            this.success = false;
            this.errorMsg = msg;
            return this;
        }

        public FilterChainMessage ok() {
            this.success = true;
            return this;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public boolean hasException() {
            return hasException;
        }

        public Exception getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "FilterChainMessage{" +
                    "success=" + success +
                    ", errorMsg='" + errorMsg + '\'' +
                    ", hasException=" + hasException +
                    ", exception=" + exception +
                    '}';
        }

    }

}
