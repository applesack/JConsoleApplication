package xyz.scootaloo.console.app.support.common;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/18 23:08
 */
public interface Console {

    // 完整的控制台配置类
    static ConsoleConfigProvider.DefaultValueConfigBuilder config() {
        return new ConsoleConfigProvider.DefaultValueConfigBuilder();
    }

    // 精简的控制台配置类
    static ConsoleConfigProvider.SimpleConfigBuilder simpleConf() {
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

}
