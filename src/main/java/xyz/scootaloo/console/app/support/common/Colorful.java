package xyz.scootaloo.console.app.support.common;

/**
 * 可以在控制台输出彩色的文本
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:25
 */
public class Colorful extends DefaultConsole {
    // 单例
    protected static final Colorful INSTANCE = new Colorful() {};

    public String green(Object line) {
        return ConsoleColor.GREEN + line;
    }

    public String white(Object line) {
        return ConsoleColor.WHITE + line;
    }

    public String red(Object line) {
        return ConsoleColor.RED + line;
    }

    public String yellow(Object line) {
        return ConsoleColor.YELLOW + line;
    }

    public String blue(Object line) {
        return ConsoleColor.BLUE + line;
    }

    public String purple(Object line) {
        return ConsoleColor.PURPLE + line;
    }

    public String grey(Object line) {
        return ConsoleColor.GRAY + line;
    }

    public String cyanogen(Object line) {
        return ConsoleColor.CYANOGEN + line;
    }

}
