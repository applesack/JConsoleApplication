package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider;
import xyz.scootaloo.console.app.config.FactoryCollector;

import java.util.function.Consumer;
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
    static FactoryCollector factories() {
        return new FactoryCollector(new ConsoleConfigProvider.DefaultValueConfigBuilder());
    }

    //---------------------------------------------------------------------------------

    void print(Object z);

    void println(Object z);

    void err(Object z);

    //---------------------------------------------------------------------------------

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

    //---------------------------------------便捷的异常处理------------------------------------------------
    /*                                使用lambda表达式 的方式调用 会抛出异常的方法                            */

    //--------------------------------------有返回值，方法有一个参数-----------------------------------------/
    static <T, R> R ex(ExProvider<T, R> provider, T input) {
        return ex(provider, input, () -> null);
    }
    static <T, R> R ex(ExProvider<T, R> provider, T input, Supplier<R> supplier) {
        return ex(provider, input, supplier, (e) -> {});
    }
    static <T, R> R ex(ExProvider<T, R> provider, T input, Supplier<R> supplier, Consumer<Exception> consumer) {
        try {
            return provider.get(input);
        } catch (Exception e) {
            consumer.accept(e);
            return supplier.get();
        }
    }
    //--------------------------------------------------------------------------------------------------/

    //--------------------------------------有返回值，方法有两个参数-----------------------------------------/
    static <T1, T2, R> R dbEx(DbExProvider<T1, T2, R> provider, T1 input1, T2 input2) {
        return dbEx(provider, input1, input2, () -> null, (e) -> {});
    }
    static <T1, T2, R> R dbEx(DbExProvider<T1, T2, R> provider, T1 input1, T2 input2,
                              Consumer<Exception> consumer) {
        return dbEx(provider, input1, input2, () -> null, consumer);
    }
    static <T1, T2, R> R dbEx(DbExProvider<T1, T2, R> provider, T1 input1, T2 input2,
                              Supplier<R> supplier, Consumer<Exception> consumer) {
        try {
            return provider.get(input1, input2);
        } catch (Exception e) {
            consumer.accept(e);
            return supplier.get();
        }
    }
    //--------------------------------------------------------------------------------------------------/

    //---------------------------------------有返回值，方法没有参数-----------------------------------------/
    static <T> T exSupplier(SimpleSupplier<T> simpleProvider) {
        return exSupplier(simpleProvider, () -> null, (ex) -> {});
    }
    static <T> T exSupplier(SimpleSupplier<T> simpleSupplier, Supplier<T> dftSupplier,
                            Consumer<Exception> exceptionConsumer) {
        try {
            return simpleSupplier.get();
        } catch (Exception e) {
            exceptionConsumer.accept(e);
            return dftSupplier.get();
        }
    }
    //--------------------------------------------------------------------------------------------------/

    // 有两个参数，无返回值
    static <T1, T2> void wDbEx(WithoutReturnVar_DB_Provider<T1, T2> provider, T1 input1, T2 input2) {
        try {
            provider.set(input1, input2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    interface ExProvider<T, R> {

        R get(T t) throws Exception;

    }

    @FunctionalInterface
    interface DbExProvider<T1, T2, R> {

        // 有返回值，两个参数，有可能抛出异常的方法
        R get(T1 t1, T2 t2) throws Exception;

    }

    @FunctionalInterface
    interface WithoutReturnVar_DB_Provider<T1, T2> {

        // 有两个参数，无返回值，有可能抛出异常的方法
        void set(T1 t1, T2 t2) throws Exception;

    }

    @FunctionalInterface
    interface SimpleSupplier<T> {

        T get() throws Exception;

    }

}
