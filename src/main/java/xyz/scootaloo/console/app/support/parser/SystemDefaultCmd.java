package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.component.StrategyFactory;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
@StrategyFactory
public class SystemDefaultCmd implements Colorful {

    @Cmd
    public void help(@Opt('-') String cmdName) {
        if (cmdName.isEmpty()) {
            for (Actuator actuator : AssemblyFactory.strategyMap.values()) {
                printInfo(actuator);
            }
        } else {
            Actuator actuator = AssemblyFactory.findInvoker(cmdName);
            printInfo(actuator);
        }
    }

    private void printInfo(Actuator actuator) {
        if (actuator instanceof AssemblyFactory.ActuatorImpl) {
            ((AssemblyFactory.ActuatorImpl) actuator).printInfo();
        }
    }

}
