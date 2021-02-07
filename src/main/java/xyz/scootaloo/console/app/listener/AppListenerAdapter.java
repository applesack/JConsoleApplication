package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.common.ConsoleMessage;
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

    @Override
    default boolean enable() {
        return true;
    }

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default boolean accept(Moment moment) {
        return false;
    }

    @Override
    default void onAppStarted(ConsoleConfig config) {

    }

    @Override
    default String onInput(String cmdline) {
        return cmdline;
    }

    @Override
    default void onResolveInput(String cmdName, List<String> cmdItems) {
    }

    @Override
    default void onMessage(ConsoleMessage message) {
    }

    @Override
    default void onInputResolved(String cmdName, InvokeInfo info) {

    }
}
