package xyz.scootaloo.console.app.common;

/**
 * 提供生成输出器的工厂
 * @author flutterdash@qq.com
 * @since 2021/2/25 0:04
 */
@FunctionalInterface
public interface CPrinterSupplier extends Factory {

    /**
     * 工厂方法，实现类的此工厂方法可能会被多次调用。<br>
     * 所以这个方法应该每次返回新的实例.
     * @return 输出器
     */
    CPrinter get();

}
