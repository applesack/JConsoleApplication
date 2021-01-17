package xyz.scootaloo.console.app.support.common;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.util.function.Supplier;

/**
 * 代理方法调用
 * 貌似没什么用了
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:20
 */
@Deprecated
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

    public static Object invoke(Class<?> supplier) {
        try {
            return ClassUtils.newInstance(supplier);
        } catch (Exception e) {
            cPrint.exit0(e.getMessage());
            return null;
        }
    }

}
