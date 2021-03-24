package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.common.Colorful;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * 测试工具
 *
 * @author flutterdash@qq.com
 * @since 2021/3/15 22:36
 */
public class Tester<In, Out> {
    private static final Colorful color = Colorful.INSTANCE;
    private final Function<In, Out> function;
    private final boolean exitOnException;
    private final List<Sample<In, Out>> samples = new ArrayList<>();

    private Function<In, String> inputConvertor;
    private Function<Out, String> outputConvertor;

    private Matcher<Out> DEFAULT_MATCHER = Object::equals;

    /**
     * 创建一个方法测试对象
     * @param function 被测试的方法
     * @param <T> 方法的输入 方法参数
     * @param <R> 方法的输出 方法返回值
     * @return 测试对象
     */
    public static <T, R> Tester<T, R> createTest(Function<T, R> function) {
        return createTest(function, false);
    }

    public static <T, R> Tester<T, R> createTest(Function<T, R> function, boolean exitOnException) {
        return new Tester<>(function, exitOnException);
    }

    private Tester(Function<In, Out> function, boolean exitOnException) {
        this.function = function;
        this.exitOnException = exitOnException;
    }

    /**
     * 添加一个测试用例
     * @param input 输入
     * @param output 预期输出
     * @return 构建者
     */
    public Tester<In, Out> addCase(In input, Out output) {
        samples.add(new Sample<>(input, output, this));
        outputConvertor = getStringConverter(output);
        inputConvertor = getStringConverter(input);
        return this;
    }

    /**
     * 设置一个匹配器
     * @param usrMatcher 用户自定义的匹配器
     * @return 构建者
     */
    public Tester<In, Out> setMatcher(Matcher<Out> usrMatcher) {
        DEFAULT_MATCHER = usrMatcher;
        return this;
    }

    /**
     * 执行测试
     * 测试结果，如果通过则绿色显示
     *         如果失败则红色显示
     */
    public void test() {
        samples.forEach(sample ->
                sample.matcher = DEFAULT_MATCHER);
        samples.forEach(sample ->
                sample.matchAndShow(function, exitOnException));
    }

    private static <T> Function<T, String> getStringConverter(T type) {
        Class<?> klass = type.getClass();
        if (klass.isArray()) {
            Class<?> componentType = klass.getComponentType();
            if (componentType == int.class)
                return (in) -> Arrays.toString((int[]) in);
            if (componentType == short.class)
                return (in) -> Arrays.toString((short[]) in);
            if (componentType == byte.class)
                return (in) -> Arrays.toString((byte[]) in);
            if (componentType == long.class)
                return (in) -> Arrays.toString((long[]) in);
            if (componentType == float.class)
                return (in) -> Arrays.toString((float[]) in);
            if (componentType == double.class)
                return (in) -> Arrays.toString((double[]) in);
            if (componentType == boolean.class)
                return (in) -> Arrays.toString((boolean[]) in);
            else
                return (in) -> Arrays.toString((Object[]) in);
        } else {
            return Object::toString;
        }
    }

    private static class Sample<In, Out> {

        final In input;
        final Out output;
        final Tester<In, Out> tester;
        Matcher<Out> matcher;



        private Sample(In input, Out output, Tester<In, Out> tester) {
            this.input = input;
            this.output = output;
            this.tester = tester;
        }

        private void matchAndShow(Function<In, Out> function, boolean exitOnException) {
            try {
              Out actualOut = function.apply(input);
              if (matcher.match(output, actualOut)) {
                  System.out.println(color.green("PASS Input : " + tester.inputConvertor.apply(input) + "\n     Output: " + tester.outputConvertor.apply(output)));
              } else {
                  System.out.println(color.red("FAIL Input : " + tester.inputConvertor.apply(input) + "\n     Output: " +tester.outputConvertor.apply(output)
                          + "\n     Actual: " + tester.outputConvertor.apply(actualOut)));
              }
            } catch (Exception e) {
                e.printStackTrace();
                if (exitOnException)
                    System.exit(0);
            }
        }

    }

    @FunctionalInterface
    public interface Matcher<T> {

        /**
         * 自定义的匹配器
         * @param sample 预期值
         * @param actual 实际值
         * @return 判断实际值是否符合预期，假如符合条件返回true，不符合返回false
         */
        boolean match(T sample, T actual);

    }

}
