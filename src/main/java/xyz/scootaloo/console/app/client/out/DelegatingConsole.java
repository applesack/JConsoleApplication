package xyz.scootaloo.console.app.client.out;

import xyz.scootaloo.console.app.client.Console;

/**
 * 一些通用的便捷方法，实现此接口可以快捷的调用
 *
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public class DelegatingConsole extends Console {
    /** singleton */
    private static volatile DelegatingConsole INSTANCE;
    private static final Object LOCK = new Object();

    private volatile CPrinter impl;

    public static DelegatingConsole getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new DelegatingConsole();
                }
            }
        }
        return INSTANCE;
    }

    private DelegatingConsole() {
        this.impl = CPrinterImpl.INSTANCE;
    }

    public void changeOutput(CPrinter printer) {
        this.impl = printer;
    }

    @Override
    public void print(Object o) {
        impl.print(o);
    }

    @Override
    public void println(Object o) {
        impl.println(o);
    }

    @Override
    public void err(Object o) {
        impl.err(o);
    }

    // 默认实现，使用标准系统标准输出
    public static void setPrinter(CPrinter cPrinter) {
        if (cPrinter != null)
            INSTANCE.impl = cPrinter;
    }

    private static class CPrinterImpl extends CPrinter {
        private static final CPrinterImpl INSTANCE = new CPrinterImpl();

        @Override
        public void print(Object o) {
            System.out.print(o);
        }

        @Override
        public void println(Object o) {
            System.out.println(o);
        }

        @Override
        public void err(Object o) {
            System.err.println(o);
        }

    }

}
