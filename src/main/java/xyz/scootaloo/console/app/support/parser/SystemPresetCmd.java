package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.listener.AppListenerAdapter;
import xyz.scootaloo.console.app.support.listener.EventPublisher;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 系统预设的命令
 * app help dis en his sleep 等
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
public class SystemPresetCmd implements Colorful, AppListenerAdapter {

    private static final Colorful cPrint = instance;
    private static ConsoleConfig config;
    private static final String version = "v0.1";
    private static final String SYS_TAG = "sys";

    @Cmd(name = "app", tag = SYS_TAG)
    private void application(@Opt('v') boolean ver) {
        if (ver)
            println("版本: " + version);
        cPrint.println("https://github.com/applesack/JConsoleApplication.git");
    }

    @Cmd(name = "hp", tag = SYS_TAG)
    private void help(@Opt(value = 'n', defVal = "all") String cmdName) {
        if (cmdName.equals("all")) {
            for (Actuator actuator : AssemblyFactory.strategyMap.values()) {
                printInfo(actuator);
            }
        } else {
            Actuator actuator = AssemblyFactory.findInvoker(cmdName);
            printInfo(actuator);
        }
    }

    @Cmd(name = "dis", tag = SYS_TAG)
    private void disable(String lisName) {
        EventPublisher.disableListener(lisName);
    }

    @Cmd(name = "en", tag = SYS_TAG)
    private void enable(String lisName) {
        EventPublisher.enableListener(lisName);
    }

    @Cmd(name = "lis", tag = SYS_TAG)
    private void listeners() {
        EventPublisher.showAllListeners();
    }

    @Cmd(name = "his", tag = SYS_TAG)
    private void history(@Opt(value = 'n', fullName = "size", defVal = "-1") int size,
                         @Opt(value = 's', fullName = "name") String name,
                         @Opt(value = 'a', fullName = "all") boolean isAll,
                         @Opt(value = 'u', fullName = "success") boolean success,
                         @Opt(value = 'r', fullName = "rtnVal") boolean rtnVal,
                         @Opt(value = 'g', fullName = "args") boolean args,
                         @Opt(value = 't', fullName = "invokeAt") boolean invokeAt,
                         @Opt(value = 'i', fullName = "interval") boolean interval) {
        History.select(name, size, isAll, success, rtnVal, args, invokeAt, interval);
    }

    @Cmd(tag = SYS_TAG)
    private void sleep(@Opt('m') int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Cmd(name = "cls", tag = SYS_TAG)
    private void clear() {
        println("清屏功能用java代码实现比较繁琐，目前暂时不考虑实现这个功能");
    }

    private void printInfo(Actuator actuator) {
        if (actuator instanceof AssemblyFactory.MethodActuator) {
            ((AssemblyFactory.MethodActuator) actuator).printInfo();
        }
    }

    // ---------------------------------监听器----------------------------------------

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
        return moment == Moment.OnInputResolved || moment == Moment.OnAppStarted;
    }

    @Override
    public void onInputResolved(String cmdName, InvokeInfo info) {
        if (info != null)
            History.add(info);
    }

    @Override
    public String info() {
        return "系统自带的监听器，将命令参数中的占位符替换成函数的返回值，另外记录执行过的命令信息";
    }


    // ---------------------------------------------------------------------------------
    // 实现历史记录功能时使用，前提条件是sys监听器已经启用
    private static class History {
        // 历史记录
        private static final LinkedList<InvokeInfo> history = new LinkedList<>();

        // 日期转换器 执行时间
        private static final SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm");

        public static void printInfo(InvokeInfo info, boolean isAll, boolean success,
                                     boolean rtnVal, boolean args,
                                     boolean invokeAt, boolean interval) {
            StringBuilder sb = new StringBuilder();
            // 执行的日期
            if (isAll || invokeAt)
                sb.append('[').append(time_sdf.format(new Date(info.getInvokeAt()))).append("] ");
            // 执行用时
            if (isAll || interval)
                sb.append('[').append(StringUtils.trimNumberSizeTo4(info.getInterval())).append("] ");
            // 命令/方法名
            sb.append("[name: ").append(info.getName()).append("] ");
            // 是否成功
            if (isAll || success)
                sb.append('[').append(info.isSuccess()).append("] ");
            // 使用的参数
            if (isAll || args)
                sb.append("[args: ").append(info.getCmdArgs()).append("] ");
            // 返回值
            if (isAll || rtnVal)
                sb.append("[rtn: ").append(info.getRtnVal()).append(']');
            cPrint.println(sb);
        }

        public static void add(InvokeInfo info) {
            if (history.size() >= config.getMaxHistory())
                history.removeFirst();
            history.addLast(info);
        }

        public static void select(String name, int size, boolean isAll, boolean success, boolean rtnVal,
                                  boolean args, boolean invokeAt, boolean interval) {
            boolean matchByName = true;
            if (name == null)
                matchByName = false;
            if (size < 0)
                size = history.size();
            else
                size = Math.min(history.size(), size);
            LinkedList<InvokeInfo> targetInfos = new LinkedList<>();
            ListIterator<InvokeInfo> it = history.listIterator(history.size());
            while (it.hasPrevious()) {
                if (size <= 0)
                    break;
                InvokeInfo info = it.previous();
                if (matchByName) {
                    if (info.getName().equals(name)) {
                        targetInfos.addFirst(info);
                        size--;
                    }
                } else {
                    targetInfos.addFirst(info);
                    size--;
                }
            }

            for (InvokeInfo inf : targetInfos) {
                printInfo(inf, isAll, success, rtnVal, args, invokeAt, interval);
            }
        }

    }

}
