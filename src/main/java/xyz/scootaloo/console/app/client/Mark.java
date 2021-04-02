package xyz.scootaloo.console.app.client;

/**
 * 暂时弃用
 *
 * 对资源的描述
 * 通过 id 编号来区别不同的唯一资源
 * 通过 重写 glob() 方法来决定此资源是否共享给其他用户
 *
 * @author flutterdash@qq.com
 * @since 2021/3/12 21:13
 */
@Deprecated
@FunctionalInterface
public interface Mark {

    int id();

    default boolean glob() {
        return true;
    }

}
