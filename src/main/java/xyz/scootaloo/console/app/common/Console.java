package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider;
import xyz.scootaloo.console.app.config.FactoryCollector;

/**
 * 抽象的控制台操作
 * @see DefaultConsole 默认实现
 * @author flutterdash@qq.com
 * @since 2021/1/18 23:08
 */
public abstract class Console extends CPrinter {

    /**
     * 完整的控制台配置类<br>
     * <p>注意：当classpath下有console.yml这个文件时，假如不想使用配置文件的配置，
     *      要调用{@code setConfFile(null)}，防止配置被文件中的配置覆盖</p>
     * @return 使用这个配置对象进行配置
     */
    public static ConsoleConfigProvider.DefaultValueConfigBuilder config() {
        return new ConsoleConfigProvider.DefaultValueConfigBuilder();
    }

    /**
     * 使用此工厂方法获取配置工厂的构建者。<br>
     * <p>一个简单的启动示例，这里演示如何将工厂注册进框架并启动一个控制台应用。<br>
     * 假设 {@code Test} 类是一个工厂，它实现了框架的某个接口。</p>
     * <pre> {@code
     * public static void main(String[] args) {
     *     ApplicationRunner.consoleApplication(
     *                  Console.factories()
     *                      .add(new Test(), true)
     *                  .ok()).run();
     * }} </pre>
     * @return 返回一个收集工厂的构建者对象，完成构建后得到一个ConsoleConfig配置类对象，此配置对象可用于启动控制台应用
     */
    public static FactoryCollector factories() {
        return new FactoryCollector(new ConsoleConfigProvider.DefaultValueConfigBuilder());
    }

    //---------------------------------------------------------------------------------

    /**
     * @see CPrinter
     * @param z -
     */
    public abstract void print(Object z);

    /**
     * @see CPrinter
     * @param z -
     */
    public abstract void println(Object z);

    /**
     * @see CPrinter
     * @param z -
     */
    public abstract void err(Object z);

    //---------------------------------------------------------------------------------

    public void onException(ConsoleConfig config, Exception e) {
        onException(config, e, null);
    }

    public void onException(ConsoleConfig config, Exception e, String msg) {
        onException(config, e, msg, false);
    }

    public void onException(ConsoleConfig config, Exception e, String msg, boolean exit) {
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
    public void exit0(String msg) {
        println(msg);
        exit0();
    }

    // 退出应用
    public void exit0() {
        println("应用退出");
        System.exit(0);
    }

}
