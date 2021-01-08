package xyz.scootaloo.console.app.support.common;

import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.DefaultValueConfigBuilder;
import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.SimpleConfig;

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
    static SimpleConfig simpleConf() {
        return new SimpleConfig();
    }

    // 精简的输出方式(不换行)
    default void print(Object line) {
        System.out.print(line);
    }

    // 精简的输出方式(换行)
    default void println(Object line) {
        System.out.println(line);
    }

    // jdk的错误输出流
    default void errPrintln(Object line) {
        System.err.println();
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
