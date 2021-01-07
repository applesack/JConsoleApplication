package xyz.scootaloo.console.app.support.common;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:25
 */
public interface Colorful extends Commons {

    Colorful instance = new Colorful() {};

    default String green(Object line) {
        return ConsoleColor.GREEN + line;
    }

    default String white(Object line) {
        return ConsoleColor.WHITE + line;
    }

    default String red(Object line) {
        return ConsoleColor.RED + line;
    }

    default String yellow(Object line) {
        return ConsoleColor.YELLOW + line;
    }

    default String blue(Object line) {
        return ConsoleColor.BLUE + line;
    }

    default String purple(Object line) {
        return ConsoleColor.PURPLE + line;
    }

    default String grey(Object line) {
        return ConsoleColor.GRAY + line;
    }

    default String cyanogen(Object line) {
        return ConsoleColor.CYANOGEN + line;
    }

}
