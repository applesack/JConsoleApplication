package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.plugin.ConsolePluginAdapter;
import xyz.scootaloo.console.app.support.plugin.EventPublisher;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
@Plugin
@CommandFactory
public class SystemPresetCmd implements Colorful, ConsolePluginAdapter {

    private static final Colorful cPrint = instance;
    private static ConsoleConfig config;

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

    @Cmd(name = "his")
    private void history(@Opt(value = 'n', defVal = "-1") int n, @Opt(value = 's') String name) {
        CmdInfo.select(name, n);
    }

    @Cmd
    private void te() throws InterruptedException {
        Thread.sleep(100);
    }

    @Cmd(name = "cls")
    private void clear() {
        println("清屏功能用java代码实现比较繁琐，目前暂时不考虑实现这个功能");
    }

    private void printInfo(Actuator actuator) {
        if (actuator instanceof AssemblyFactory.ActuatorImpl) {
            ((AssemblyFactory.ActuatorImpl) actuator).printInfo();
        }
    }

    // ---------------------------------插件实现----------------------------------------

    @Override
    public String getName() {
        return "sys";
    }

    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public void onAppStarted(ConsoleConfig conf) {
        config = conf;
    }

    @Override
    public boolean accept(Moment moment) {
        return moment == Moment.OnResolveInput || moment == Moment.OnAppStarted ||
                moment == Moment.OnInputResolved;
    }

    @Override
    public void onResolveInput(String cmdName, List<String> cmdItems) {
        CmdInfo.start(cmdName, cmdItems);
    }

    @Override
    public void onInputResolved(String cmdName, Object rtnVal) {
        CmdInfo.end();
    }

    @Override
    public String info() {
        return "系统自带的插件，将命令参数中的占位符替换成函数的返回值，另外记录执行过的命令信息";
    }



    // ---------------------------------------------------------------------------------

    private static class CmdInfo {
        private static final LinkedList<CmdInfo> history = new LinkedList<>();

        private static final SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        private final String name;
        private final String time;

        private final String args;
        private long interval;

        private CmdInfo(String name, List<String> args) {
            this.args = (args == null ? "" : String.join(" ", args)).trim();
            this.name = name;
            this.time = sdf.format(new Date());
            this.interval = System.currentTimeMillis();
        }

        public void printInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(time).append(" ").append(StringUtils.trimNumberSizeTo4(interval))
                    .append("] [").append(name).append(" ").append(args).append("]");
            cPrint.println(sb);
        }

        public static void start(String name, List<String> args) {
            if (history.size() >= config.getMaxHistory())
                history.removeFirst();
            history.addLast(new CmdInfo(name, args));
        }

        public static void end() {
            CmdInfo info = history.getLast();
            info.interval = System.currentTimeMillis() - info.interval;
        }

        public static void select(String name, int size) {
            boolean matchByName = true;
            if (name == null)
                matchByName = false;
            if (size < 0)
                size = history.size();
            else
                size = Math.min(history.size(), size);
            LinkedList<CmdInfo> targetInfos = new LinkedList<>();
            ListIterator<CmdInfo> it = history.listIterator(history.size());
            while (it.hasPrevious()) {
                if (size <= 0)
                    break;
                CmdInfo info = it.previous();
                if (matchByName) {
                    if (info.name.equals(name)) {
                        targetInfos.addFirst(info);
                        size--;
                    }
                } else {
                    targetInfos.addFirst(info);
                    size--;
                }
            }

            for (CmdInfo inf : targetInfos) {
                inf.printInfo();
            }
        }

    }

}
