package xyz.scootaloo.console.app.support.common;

import java.util.function.Supplier;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:20
 */
public class ProxyInvoke {
    private static final Colorful cPrint = Colorful.instance;

    public static <T> T invoke(Supplier<T> supplier, String msg) {
        try {
            return supplier.get();
        } catch (Exception e) {
            cPrint.exit0(msg);
            return null;
        }
    }

    public static <T> T invoke(Class<T> supplier) {
        try {
            return supplier.newInstance();
        } catch (Exception e) {
            cPrint.exit0("类未提供无参的public构造方法，无法通过反射实例化: " + supplier.getSimpleName());
            return null;
        }
    }

}
