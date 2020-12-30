package xyz.scootaloo.console.app.support.plugin;

import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:30
 */
public interface ConsolePlugin {

    String getName();

    int getOrder();

    boolean accept(Moment moment);

    void onAppStarted(ConsoleConfig config);

    String onInput(String cmdline);

    void onResolveInput(List<String> cmdItems);

    void onInputResolved(String cmdName, Object rtnVal);

}
