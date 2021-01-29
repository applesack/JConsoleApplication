package xyz.scootaloo.console.app.common;

import java.util.Scanner;

/**
 * 资源管理，管理一些常用可通用的资源，例如单例
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:42
 */
public enum ResourceManager {

    ;

    static Scanner SCANNER = new Scanner(System.in);
    static ClassLoader LOADER = ResourceManager.class.getClassLoader();
    static Console CONSOLE = DefaultConsole.INSTANCE;

    public static Console getConsole() {
        return CONSOLE;
    }

    public static ClassLoader getLoader() {
        return LOADER;
    }

    public static Scanner getScanner() {
        return SCANNER;
    }

    public static void setConsole(Console console) {
        CONSOLE = console;
    }

    public static Colorful getColorfulPrinter() {
        return Colorful.INSTANCE;
    }

}
