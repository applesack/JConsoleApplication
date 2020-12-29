package xyz.scootaloo.console.app.support.common;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public interface OutputCommons {

    default void print(Object line) {
        System.out.print(line);
    }

    default void println(Object line) {
        System.out.println(line);
    }

    default void errPrintln(Object line) {
        System.err.println();
    }

    default void exit0(String msg) {
        println(msg);
        exit0();
    }

    default void exit0() {
        println("应用退出");
        System.exit(0);
    }

}
