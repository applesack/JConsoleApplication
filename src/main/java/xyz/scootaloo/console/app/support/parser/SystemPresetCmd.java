package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.plugin.ConsolePluginAdapter;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
@Plugin
@CommandFactory
public class SystemPresetCmd implements Colorful, ConsolePluginAdapter {

    @Cmd(name = "find")
    private void help(@Opt(value = '*', defVal = "all") String cmdName) {
        if (cmdName.equals("all")) {
            for (Actuator actuator : AssemblyFactory.strategyMap.values()) {
                printInfo(actuator);
            }
        } else {
            Actuator actuator = AssemblyFactory.findInvoker(cmdName);
            printInfo(actuator);
        }
    }

    @Cmd(name = "dis")
    private void disable(String plgName) {
        EventPublisher.disablePlugin(plgName);
    }

    @Cmd(name = "en")
    private void enable(String plgName) {
        EventPublisher.enablePlugin(plgName);
    }

    @Cmd(name = "plgs")
    private void plugins() {
        EventPublisher.showAllPlugins();
    }

    private void printInfo(Actuator actuator) {
        if (actuator instanceof AssemblyFactory.ActuatorImpl) {
            ((AssemblyFactory.ActuatorImpl) actuator).printInfo();
        }
    }

    // ---------------------------------插件实现----------------------------------------

    @Override
    public String getName() {
        return "func";
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public boolean accept(Moment moment) {
        return moment == Moment.OnResolveInput;
    }

    @Override
    public void onResolveInput(List<String> cmdItems) {

    }

    // ---------------------------------------------------------------------------------
}
