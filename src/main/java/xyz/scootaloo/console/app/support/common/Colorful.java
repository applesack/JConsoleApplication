package xyz.scootaloo.console.app.support.common;

import xyz.scootaloo.console.app.support.component.ConsoleColor;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:25
 */
public class Colorful extends OutputCommons {

    protected static String green(Object line) {
        return ConsoleColor.GREEN + line;
    }

    protected static String white(Object line) {
        return ConsoleColor.WHITE + line;
    }

    protected static String red(Object line) {
        return ConsoleColor.RED + line;
    }

    protected static String yellow(Object line) {
        return ConsoleColor.YELLOW + line;
    }

    protected static String blue(Object line) {
        return ConsoleColor.BLUE + line;
    }

    protected static String purple(Object line) {
        return ConsoleColor.PURPLE + line;
    }

    protected static String grey(Object line) {
        return ConsoleColor.GRAY + line;
    }

    protected static String cyanogen(Object line) {
        return ConsoleColor.CYANOGEN + line;
    }

}
