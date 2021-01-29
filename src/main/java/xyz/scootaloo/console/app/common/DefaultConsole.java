package xyz.scootaloo.console.app.common;

/**
 * 一些通用的便捷方法，实现此接口可以快捷的调用
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public interface DefaultConsole extends Console {

    DefaultConsole INSTANCE = new DefaultConsole() {};

    default void print(Object z) {
        System.out.print(z);
    }

    default void println(Object z) {
        System.out.println(z);
    }

}
