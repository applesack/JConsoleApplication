package xyz.scootaloo.console.app.application.processor;

/**
 * 框架中通用的无参无返回值的简单回调接口
 * @author flutterdash@qq.com
 * @since 2021/3/3 21:49
 */
@FunctionalInterface
public interface CallBack {

    void call();

    /**
     * 对于这些回调，可以重写这个方法来指定优先级
     * @return 优先级，用于排序，默认数值越小优先级越高
     */
    default int getOrder() {
        return 5;
    }

}
