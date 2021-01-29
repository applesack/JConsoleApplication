package xyz.scootaloo.console.app.common;

/**
 * 可以在控制台输出彩色的文本
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:25
 */
public class Colorful implements Console {
    // 单例
    protected static final Colorful INSTANCE = new Colorful() {};

    private static final Console console = ResourceManager.getConsole();

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

    @Override
    public void print(Object z) {
        console.print(z);
    }

    @Override
    public void println(Object z) {
        console.println(z);
    }
}
