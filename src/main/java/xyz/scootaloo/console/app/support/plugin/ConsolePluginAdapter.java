package xyz.scootaloo.console.app.support.plugin;

import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.List;

/**
 * 插件接口的适配器
 * @see xyz.scootaloo.console.app.support.plugin.ConsolePlugin
 * @author flutterdash@qq.com
 * @since 2020/12/30 10:26
 */
public interface ConsolePluginAdapter extends ConsolePlugin {

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

    default void onInputResolved(String cmdName, Object rtnVal) {

    }
}