package xyz.scootaloo.console.app.util;

import xyz.scootaloo.console.app.parser.MethodMeta;
import xyz.scootaloo.console.app.parser.ParameterParser;
import xyz.scootaloo.console.app.parser.ResultWrapper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义解析器专用工具
 *
 * 快速生成解析器所需数据
 *
 * @author flutterdash@qq.com
 * @since 2021/3/8 10:20
 */
public class ParserTestUtils {

    /**
     * 获取测试器对象
     * @param parser 解析器
     * @param methodName 方法名称，调用 {@code getTester} 这个方法所在类的一个实例方法的方法名
     * @return 测试器对象
     */
    public static ParserTester getTester(ParameterParser parser, String methodName) {
        Object callerInstance = ClassUtils.instance(false);
        if (callerInstance == null)
            throw new RuntimeException("无法实例化当前类");
        Class<?> callerClass = callerInstance.getClass();
        LinkedList<Method> methods = new LinkedList<>();
        for (Method method : callerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName))
                methods.add(method);
        }
        if (methods.size() == 1) {
            System.out.println(ClassUtils.getMethodDescribe(methods.peek()));
            return new ParserTester(parser, methods.pop(), callerInstance);
        }
        throw new RuntimeException("方法名重载，无法确定被测试的方法对象");
    }

    /**
     * 获取调用者的一个方法对象
     * @param methodName 当前类的一个方法名，假如有多个同名方法，将返回最先找到的方法
     * @return 方法对应的 {@code method} 对象
     */
    public static Method getMethodByName(String methodName) {
        Object callerInstance = ClassUtils.instance(false);
        if (callerInstance == null)
            throw new RuntimeException("无法实例化当前类");
        Class<?> callerClass = callerInstance.getClass();
        for (Method method : callerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName))
                return method;
        }
        throw new IllegalArgumentException("没有找到于此方法名对应的方法");
    }

    /**
     * Tester
     */
    public static class ParserTester {
        private final List<String> commands = new ArrayList<>();
        private final MethodMeta meta;
        private final Method method;
        private final Object obj;
        private final ParameterParser parser;

        private ParserTester(ParameterParser parser , Method method, Object obj) {
            this.meta =  MethodMeta.getInstance(method, obj);
            this.method = method;
            this.obj = obj;
            this.parser = parser;
        }

        /**
         * 添加需要测试的命令行
         * @param command 命令行
         * @return 构建者
         */
        public ParserTester addTestCommand(String command) {
            commands.add(command);
            return this;
        }

        /**
         * 开始测试，这里将会按照顺序执行这些添加进来的命令行
         * @throws Exception 执行过程中各种可能的异常
         */
        public void test() throws Exception {
            // 由解析工厂将字符串命令解析成Object数组供method对象调用，结果由wrapper包装
            for (String command : commands) {
                ResultWrapper wrapper;
                wrapper = parser.parse(meta, getArgList(command));
                // 如果解析成功
                if (wrapper.isSuccess()) {
                    method.setAccessible(true);
                    // 用解析后的参数对method进行调用
                    method.invoke(obj, wrapper.getArgs());
                }
                // 解析失败: 这里一般是命令行中缺省了必要参数，或者命令行不完整等
                else {
                    throw wrapper.getEx();
                }
            }
        }

        // 得到按空格分割的字符串列表
        private List<String> getArgList(String command) {
            return Arrays.stream(command.split(" ")).collect(Collectors.toList());
        }

    }

}
