package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.common.Colorful;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 测试工具
 * @author flutterdash@qq.com
 * @since 2021/3/15 22:36
 */
public class Tester<T, R> {
    private static final Colorful color = Colorful.INSTANCE;
    private final Function<T, R> function;
    private final boolean exitOnException;
    private final List<Sample<T, R>> samples = new ArrayList<>();

    private Matcher<R> DEFAULT_MATCHER = Object::equals;

    private Tester(Function<T, R> function, boolean exitOnException) {
        this.function = function;
        this.exitOnException = exitOnException;
    }

    public Tester<T, R> addCase(T input, R output) {
        samples.add(new Sample<>(input, output));
        return this;
    }

    public Tester<T, R> setMatcher(Matcher<R> usrMatcher) {
        DEFAULT_MATCHER = usrMatcher;
        return this;
    }

    public void test() {
        samples.forEach(sample ->
                sample.matcher = DEFAULT_MATCHER);
        samples.forEach(sample ->
                sample.matchAndShow(function, exitOnException));
    }

    public static <T, R> Tester<T, R> createTest(Function<T, R> function) {
        return createTest(function, false);
    }

    public static <T, R> Tester<T, R> createTest(Function<T, R> function, boolean exitOnException) {
        return new Tester<>(function, exitOnException);
    }

    private static class Sample<In, Out> {

        In input;
        Out output;
        Matcher<Out> matcher;

        public Sample(In input, Out output) {
            this.input = input;
            this.output = output;
        }

        public void matchAndShow(Function<In, Out> function, boolean exitOnException) {
            try {
              Out actualOut = function.apply(input);
              if (matcher.match(output, actualOut)) {
                  System.out.println(color.green("PASS Input: " + input + "\n     Output: " + output));
              } else {
                  System.out.println(color.red("FAIL Input: " + input + "\n     Output: " + output
                          + "\n     Actual: " + actualOut));
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

        boolean match(T sample, T actual);

    }

}
