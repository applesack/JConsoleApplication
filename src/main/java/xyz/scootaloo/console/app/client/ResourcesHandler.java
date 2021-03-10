package xyz.scootaloo.console.app.client;

import java.util.function.Consumer;

/**
 * 定义销毁资源的方式
 *
 * @author flutterdash@qq.com
 * @since 2021/3/4 9:34
 */
public class ResourcesHandler {

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
    public String toString() {
        return "ResourcesHandler{" +
                "resourcesMark='" + resourcesMark + '\'' +
                ", callback=" + callback +
                '}';
    }

}
