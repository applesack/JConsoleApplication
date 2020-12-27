package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.common.Colorful;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:33
 */
public abstract class ApplicationConfig extends Colorful {

    /**
     * 获取调用此方法的调用者，并实创建调用者的实例
     * @return 调用者的实例
     */
    public static Object instance() {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        String invoker = callStack[2].getClassName();
        try {
            return Class.forName(invoker);
        } catch (ClassNotFoundException e) {
            println(grey("解析异常，无法实例化类: ") + red(invoker));
            exit0();
            return null;
        }
    }

}
