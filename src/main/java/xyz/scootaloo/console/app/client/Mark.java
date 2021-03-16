package xyz.scootaloo.console.app.client;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/12 21:13
 */
@FunctionalInterface
public interface Mark {

    int id();

    default boolean glob() {
        return true;
    }

}
