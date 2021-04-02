package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.client.Console;
import xyz.scootaloo.console.app.client.out.CPrinter;
import xyz.scootaloo.console.app.client.out.CPrinterSupplier;
import xyz.scootaloo.console.app.client.out.DelegatingConsole;
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

    /** Singletons */
    static volatile           Scanner SCANNER;
    static volatile       ClassLoader LOADER;
    static volatile DelegatingConsole CONSOLE;
    static volatile          Colorful COLORFUL;
    static volatile            Random RANDOM;
    static volatile CPrinterSupplier cPrinterFactory = BackstageTaskManager::getPrinter;

    static final Object LOCK = new Object();

    // getter

    public static Console getConsole() {
        return delegatingConsole();
    }

    private static DelegatingConsole delegatingConsole() {
        if (CONSOLE == null) {
            synchronized (LOCK) {
                if (CONSOLE == null) {
                    CONSOLE = DelegatingConsole.getInstance();
                }
            }
        }
        return CONSOLE;
    }

    public static ClassLoader getLoader() {
        if (LOADER == null) {
            synchronized (LOCK) {
                if (LOADER == null) {
                    LOADER = ResourceManager.class.getClassLoader();
                }
            }
        }
        return LOADER;
    }

    public static Scanner getScanner() {
        if (SCANNER == null) {
            synchronized (LOCK) {
                if (SCANNER == null) {
                    SCANNER = new Scanner(System.in);
                }
            }
        }
        return SCANNER;
    }

    public static Colorful getColorful() {
        if (COLORFUL == null) {
            synchronized (LOCK) {
                if (COLORFUL == null) {
                    COLORFUL = new Colorful();
                }
            }
        }
        return COLORFUL;
    }

    public static Random getRandom() {
        if (RANDOM == null) {
            synchronized (LOCK) {
                if (RANDOM == null) {
                    RANDOM = ThreadLocalRandom.current();
                }
            }
        }
        return RANDOM;
    }

    public static CPrinter getPrinter() {
        return cPrinterFactory.get();
    }

    // setter

    public static void setConsole(CPrinter printer) {
        if (printer != null)
            delegatingConsole().changeOutput(printer);
    }

    public static void setPrinterFactory(CPrinterSupplier supplier) {
        if (supplier != null)
            cPrinterFactory = supplier;
    }

}
