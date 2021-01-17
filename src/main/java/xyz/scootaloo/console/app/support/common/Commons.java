package xyz.scootaloo.console.app.support.common;

import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.DefaultValueConfigBuilder;
import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.SimpleConfigBuilder;

/**
 * 一些通用的便捷方法，实现此接口可以快捷的调用
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public interface Commons {

    // 完整的控制台配置类
    static DefaultValueConfigBuilder config() {
        return new DefaultValueConfigBuilder();
    }

    // 精简的控制台配置类
    static SimpleConfigBuilder simpleConf() {
        return new SimpleConfigBuilder();
    }

    // 精简的输出方式(不换行)
    default void print(Object line) {
        System.out.print(line);
    }

    // 精简的输出方式(换行)
    default void println(Object line) {
        System.out.println(line);
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

}
