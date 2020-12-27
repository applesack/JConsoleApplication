package xyz.scootaloo.console.app.support.common;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:32
 */
public class InputCommons {

    protected static void print(Object line) {
        System.out.print(line);
    }

    protected static void println(Object line) {
        System.out.println(line);
    }

    protected static void errPrint(Object line) {
        System.err.println();
    }

    protected static void exit0() {
        System.out.println("应用退出");
        System.exit(0);
    }

}
