package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.AppListener;
import xyz.scootaloo.console.app.anno.Moment;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.List;

/**
 * 插件接口的适配器
 * @see AppListener
 * @author flutterdash@qq.com
 * @since 2020/12/30 10:26
 */
public interface AppListenerAdapter extends AppListener {

    default boolean enable() {
        return true;
    }

    default int getOrder() {
        return 0;
    }

    default boolean accept(Moment moment) {
        return false;
    }

    default void onAppStarted(ConsoleConfig config) {

    }

    default String onInput(String cmdline) {
        return cmdline;
    }

    default void onResolveInput(String cmdName, List<String> cmdItems) {

    }

    default void onInputResolved(String cmdName, InvokeInfo info) {

    }
}
