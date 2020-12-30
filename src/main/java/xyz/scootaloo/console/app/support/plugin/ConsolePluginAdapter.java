package xyz.scootaloo.console.app.support.plugin;

import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/30 10:26
 */
public interface ConsolePluginAdapter extends ConsolePlugin {

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

    default void onResolveInput(List<String> cmdItems) {

    }

    default void onInputResolved(String cmdName, Object rtnVal) {

    }
}
