package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.common.ConsoleMessage;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.List;

/**
 * 监听器接口的适配器
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
    default void onInputResolved(String cmdName, InvokeInfo info) {
    }

    @Override
    default void onMessage(ConsoleMessage message) {
    }

}
