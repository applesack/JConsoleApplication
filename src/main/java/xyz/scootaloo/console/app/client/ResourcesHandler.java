package xyz.scootaloo.console.app.client;

import java.util.function.Consumer;

/**
 * 定义销毁资源的方式
 *
 * @author flutterdash@qq.com
 * @since 2021/3/4 9:34
 */
public class ResourcesHandler implements AutoCloseable {

    private final String resourcesMark;      // 资源的标记
    private final Consumer<String> callback; // 处理此资源的回调

    protected ResourcesHandler(String resourcesMark, Consumer<String> callback) {
        this.resourcesMark = resourcesMark;
        this.callback = callback;
    }

    public void shutdown() {
        callback.accept(resourcesMark);
    }

    @Override
    public void close() {
        System.out.println("shutdown");
        shutdown();
    }

    @Override
    public String toString() {
        return "ResourcesHandler{" +
                "resourcesMark='" + resourcesMark + '\'' +
                ", callback=" + callback +
                '}';
    }

}
