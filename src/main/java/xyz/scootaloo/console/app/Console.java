package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.common.DefaultConsole;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider;

import java.util.function.Supplier;

/**
 * 抽象的控制台操作
 * 默认实现
 * @see DefaultConsole
 * @author flutterdash@qq.com
 * @since 2021/1/18 23:08
 */
public interface Console {

    // 完整的控制台配置类
    static ConsoleConfigProvider.DefaultValueConfigBuilder config() {
        return new ConsoleConfigProvider.DefaultValueConfigBuilder();
    }

    // 精简的控制台配置类
    static ConsoleConfigProvider.SimpleConfigBuilder factories() {
        return new ConsoleConfigProvider.SimpleConfigBuilder();
    }

    void print(Object z);

    void println(Object z);

    default void onException(ConsoleConfig config, Exception e) {
        onException(config, e, null);
    }

    default void onException(ConsoleConfig config, Exception e, String msg) {
        onException(config, e, msg, false);
    }

    default void onException(ConsoleConfig config, Exception e, String msg, boolean exit) {
        if (msg != null) {
            println(msg);
        } else {
            println(e.getMessage());
        }

        if (config.isPrintStackTraceOnException())
            e.printStackTrace();
        if (exit)
            exit0();
    }

    // 输出信息后退出应用
    default void exit0(String msg) {
        println(msg);
        exit0();
    }

    // 退出应用
    default void exit0() {
        println("应用退出");
        System.exit(0);
    }

    // 使用lambda表达式 的方式调用 会抛出异常的方法
    static <T, R> R ex(ExProvider<T, R> provider, T input) {
        return ex(provider, input, () -> null);
    }

    // 当遇到异常时，可以提供一个默认值
    static <T, R> R ex(ExProvider<T, R> provider, T input, Supplier<R> supplier) {
        try {
            return provider.get(input);
        } catch (Exception e) {
            return supplier.get();
        }
    }

    @FunctionalInterface
    interface ExProvider<T, R> {

        R get(T t) throws Exception;

    }

    // 两个参数的异常包装方法
    static <T1, T2, R> R dbEx(DbExProvider<T1, T2, R> provider, T1 input1, T2 input2) {
        return dbEx(provider, input1, input2, () -> null);
    }

    // 提供一个默认值
    static <T1, T2, R> R dbEx(DbExProvider<T1, T2, R> provider, T1 input1, T2 input2, Supplier<R> supplier) {
        try {
            return provider.get(input1, input2);
        } catch (Exception e) {
            return supplier.get();
        }
    }

    /**
     * 调用需要有两个参数的无返回值方法，而这个方法会抛出异常，但是又不想用try-catch时
     * Console.wDbEx(obj::method, param1, param2)
     * @param provider 有两个参数的无返回值方法
     * @param input1 这个方法的第一个参数
     * @param input2 这个方法的第二个参数
     * @param <T1> 第一个参数的类型 忽略
     * @param <T2> 第二个参数的类型 忽略
     */
    static <T1, T2> void wDbEx(WithoutReturnVar_DB_Provider<T1, T2> provider, T1 input1, T2 input2) {
        try {
            provider.set(input1, input2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    interface DbExProvider<T1, T2, R> {

        R get(T1 t1, T2 t2) throws Exception;

    }

    interface WithoutReturnVar_DB_Provider<T1, T2> {

        void set(T1 t1, T2 t2) throws Exception;

    }

}
