package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.support.BackstageTaskManager;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 资源管理，管理一些常用可通用的资源，例如单例
 *
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:42
 */
public enum ResourceManager {

    ;

    static Scanner SCANNER = new Scanner(System.in);
    static ClassLoader LOADER = ResourceManager.class.getClassLoader();
    static Console CONSOLE = DefaultConsole.INSTANCE;
    static Random random = ThreadLocalRandom.current();
    static CPrinterSupplier cPrinterFactory = BackstageTaskManager::getPrinter;

    // getter

    public static Console getConsole() {
        return CONSOLE;
    }

    public static ClassLoader getLoader() {
        return LOADER;
    }

    public static Scanner getScanner() {
        return SCANNER;
    }

    public static Colorful getColorful() {
        return Colorful.INSTANCE;
    }

    public static Random getRandom() {
        return random;
    }

    public static CPrinter getPrinter() {
        return cPrinterFactory.get();
    }

    // setter

    public static void setConsole(Console console) {
        if (console != null)
            CONSOLE = console;
    }

    public static void setPrinterFactory(CPrinterSupplier supplier) {
        if (supplier != null)
            cPrinterFactory = supplier;
    }

}
