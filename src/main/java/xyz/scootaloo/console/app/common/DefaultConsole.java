package xyz.scootaloo.console.app.common;

/**
 * 一些通用的便捷方法，实现此接口可以快捷的调用
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public class DefaultConsole extends Console {
    /** singleton */
    protected static final DefaultConsole INSTANCE = new DefaultConsole() {};
    private CPrinter default_printer = CPrinterImpl.INSTANCE;

    @Override
    public void print(Object z) {
        default_printer.print(z);
    }

    @Override
    public void println(Object z) {
        default_printer.println(z);
    }

    @Override
    public void err(Object z) {
        default_printer.err(z);
    }

    // 默认实现，使用标准系统标准输出
    public static void setPrinter(CPrinter cPrinter) {
        if (cPrinter != null)
            INSTANCE.default_printer = cPrinter;
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
