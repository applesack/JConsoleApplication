package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider;
import xyz.scootaloo.console.app.config.FactoryCollector;

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
        if (config.isPrintStackTraceOnException()) {
            e.printStackTrace();
        } else {
            if (msg != null) {
                println(msg);
            } else {
                println(e.getMessage());
            }
        }
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

}
