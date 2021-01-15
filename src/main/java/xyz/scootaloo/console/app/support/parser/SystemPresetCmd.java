package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.listener.AppListenerAdapter;
import xyz.scootaloo.console.app.support.listener.EventPublisher;
import xyz.scootaloo.console.app.support.parser.AssemblyFactory.MethodActuator;
import xyz.scootaloo.console.app.support.utils.ClassUtils;
import xyz.scootaloo.console.app.support.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统预设的命令，可以使用 find -t sys 命令查看到
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
public class SystemPresetCmd implements Colorful, AppListenerAdapter {

    private static final Colorful cPrint = instance;
    private static ConsoleConfig config;

    private static final String version = "v0.1";
    protected static final String SYS_TAG = "sys";

    // 使用方法返回值做为属性资源
    private byte setOpen = 2;
    private String propKey;

    @Cmd(name = "app", tag = SYS_TAG)
    private void application(@Opt('v') boolean ver) {
        if (ver)
            println("版本: " + version);
        cPrint.println("https://github.com/applesack/JConsoleApplication.git");
    }

    @Cmd(name = "man", tag = SYS_TAG)
    private void help(@Opt(value = 's', fullName = "name") String cmdName) {
        Actuator actuator;
        if (cmdName == null)
            actuator = AssemblyFactory.findActuator("help");
        else
            actuator = AssemblyFactory.findActuator(cmdName);
        printInfo(actuator);
    }

    @Cmd(name = "fd", tag = SYS_TAG)
    private void find(@Opt(value = 's', fullName = "name") String name,
                      @Opt(value = 't', fullName = "tag") String tag) {
        List<MethodActuator> actuatorList = AssemblyFactory.getAllCommands();
        if (name == null && tag == null) {
            actuatorList.forEach(methodActuator -> println(methodActuator.getCmdName() + " " + methodActuator.getCmd().name()));
        } else {
            if (name != null) {
                name = name.toLowerCase(Locale.ROOT);
                String finalName = name;
                List<MethodActuator> retainList = actuatorList.stream()
                        .filter(act -> act.getCmdName().equals(finalName) ||
                        act.getCmd().name().equals(finalName)).collect(Collectors.toList());
                if (retainList.isEmpty()) {
                    println("没有这个命令的信息: `" + name + "`");
                    return;
                }
                retainList.forEach(this::find);
            } else {
                List<MethodActuator> retainList = actuatorList.stream()
                        .filter(act -> act.getCmd().tag().equals(tag)).collect(Collectors.toList());
                if (retainList.isEmpty()) {
                    println("没有这个标签的信息: `" + tag + "`");
                    return;
                }
                retainList.forEach(this::find);
            }
        }
    }

    private void find(MethodActuator methodActuator) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        // 输出 标签 类名.方法名 方法参数
        sb.append('[').append(methodActuator.getCmd().tag()).append(']').append(' ')
                .append(methodActuator.getInstance().getClass().getSimpleName()).append('.')
                .append(ClassUtils.getMethodInfo(methodActuator.getMethod()));
        println(sb);
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
    private void history(@Opt(value = 'n', fullName = "size", dftVal = "-1") int size,
                         @Opt(value = 's', fullName = "name") String name,
                         @Opt(value = 'a', fullName = "all" ) boolean isAll,
                         @Opt(value = 'u', fullName = "success" ) boolean success,
                         @Opt(value = 'r', fullName = "rtnVal"  ) boolean rtnVal,
                         @Opt(value = 'g', fullName = "args"    ) boolean args,
                         @Opt(value = 't', fullName = "invokeAt") boolean invokeAt,
                         @Opt(value = 'i', fullName = "interval") boolean interval) {
        History.select(name, size, isAll, success, rtnVal, args, invokeAt, interval);
    }

    @Cmd(tag = SYS_TAG)
    private void sleep(@Opt('m') int millis) throws InterruptedException {
        if (millis >= 0)
        Thread.sleep(millis);
    }

    @Cmd(name = "cls", tag = SYS_TAG)
    private void clear() {
        println("清屏功能用java代码实现比较繁琐，目前暂时不考虑实现这个功能");
    }

    private void printInfo(Actuator actuator) {
        if (actuator instanceof MethodActuator) {
            ((MethodActuator) actuator).printInfo();
        } else {
            cPrint.println("系统中没有这个命令.");
        }
    }

    @Cmd(tag = SYS_TAG)
    private void set(@Opt(value = 'k', fullName = "key") String key,
                     @Opt(value = 'v', fullName = "value") String value) {
        if (!config.isEnablePlaceholder()) {
            println(PropertyManager.msg);
            return;
        }
        if (key == null) {
            println("未选中键");
            return;
        }
        if (key.startsWith(".")) {
            PropertyManager.set(".", null);
            return;
        }
        if (value != null) {
            PropertyManager.set(key, value);
        } else {
            setOpen = 1;
            propKey = key;
        }
    }

    @Cmd(tag = SYS_TAG)
    private void get(@Opt(value = 'k', fullName = "key") String key) {
        Object val = PropertyManager.get(key);
        if (val == null) {
            println("没有这个键的信息");
        } else {
            println(val);
        }
    }

    @Cmd(tag = SYS_TAG)
    private void keys() {
        PropertyManager.getKVs().forEach((k, v) -> {
            println("[" + k + "]: " + v);
        });
    }

    @Cmd(tag = SYS_TAG)
    private void echo(@Opt('v') String val) {
        if (val != null)
            println(val);
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
        return moment == Moment.OnInputResolved || moment == Moment.OnAppStarted ||
                moment == Moment.OnResolveInput;
    }

    @Override
    public void onResolveInput(String cmdName, List<String> cmdItems) {
        for (int i = 0; i<cmdItems.size(); i++) {
            cmdItems.set(i, PropertyManager.resolvePlaceholders(cmdItems.get(i)));
        }
    }

    @Override
    public void onInputResolved(String cmdName, InvokeInfo info) {
        if (info != null) {
            if (setOpen <= 1) {
                if (setOpen <= 0) {
                    if (info.isSuccess() && info.getRtnType() != void.class)
                        PropertyManager.set(propKey, info.get());
                    else
                        println("返回值无效，请重新设置 cmd:[" + info.getName() + "] " +
                                "args:[" + String.join(",", info.getCmdArgs()) + "]");
                    setOpen = 2;
                } else {
                    setOpen--;
                }
            }
            History.add(info);
        }
    }

    @Override
    public String info() {
        return "系统自带的监听器，将命令参数中的占位符替换成函数的返回值，另外记录执行过的命令信息";
    }

    //----------------------------------------------------------------------------------

    // 实现历史记录功能时使用，前提条件是sys监听器已经启用
    private static class History {
        // 历史记录
        private static final LinkedList<InvokeInfo> history = new LinkedList<>();
        // 日期转换器 执行时间
        private static final SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm");

        // 向容器增加新的命令
        public static void add(InvokeInfo info) {
            if (history.size() >= config.getMaxHistory())
                history.removeFirst();
            history.addLast(info);
        }

        // 筛选出符合条件的记录，并按照规则显示出来
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

        // 显示这些信息
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
            sb.append("[name: ").append(StringUtils.trimSizeTo7(info.getName())).append("] ");
            // 是否成功
            if (isAll || success)
                sb.append('[').append(info.isSuccess() ? "success" : "failed_").append("] ");
            // 使用的参数
            if (isAll || args)
                sb.append("[args: ").append(String.join(" ", info.getCmdArgs())).append("] ");
            // 返回值
            if (isAll || rtnVal)
                sb.append("[rtn: ").append(info.getRtnVal()).append(']');
            cPrint.println(sb);
        }

    }

    //--------------------------------------------------------------------------------

    // 对于系统预置命令的描述
    public static class Help {
        // 单例
        public static final Help INSTANCE = new Help();

        public String _app() {
            return "应用信息\n" +
                    "[-v][--version] 查看应用的版本信息\n";
        }

        public String _help() {
            return "帮助\n" +
                    "[-s][--name] 查询某命令的用法\n" +
                    "示例，查询history这个命令的用法: \n" +
                    "       help -s history\n" +
                    "提示: 这里的`[-s]`和`[--name]`在同一行，表示同一个参数的不同写法，一个是简写一个是全称\n" +
                    "help -s help 等于 help --name help\n";
        }

        public String _find() {
            return "查找某命令的信息\n" +
                    "[-s][--name] 根据名称查找命令\n" +
                    "[-t][--tag]  根据标签查找命令\n" +
                    "示例，查找history命令的信息\n" +
                    "       find -s history\n" +
                    "示例，查找sys标签的命令\n" +
                    "       find -t sys\n";
        }

        public String _enable() {
            return "启用监听器\n" +
                    "示例，启用名为sys的监听器\n" +
                    "       en sys\n";
        }

        public String _disable() {
            return "移除某正在监听的监听器\n" +
                    "示例，移除名为sys的监听器\n" +
                    "       dis sys";
        }

        public String _listeners() {
            return "查看当前正在启用的监听器\n" +
                    "无需参数\n";
        }

        public String _sleep() {
            return "休眠当前程序一段时间\n" +
                    "[-m] 单位毫秒\n" +
                    "示例，休眠当前程序100毫秒\n" +
                    "       sleep -m 100\n" +
                    "或者\n" +
                    "       sleep 100\n";
        }

        public String _history() {
            return "查询历史记录\n" +
                    "[-n][--size]     需要展示历史记录的数量，显示最近的记录\n" +
                    "[-s][--name]     查找指定的命令调用记录\n" +
                    "[-a][--all]      显示命令调用的所有信息\n" +
                    "[-u][--success]  显示命令调用是否成功\n" +
                    "[-r][--rtnVal]   显示命令调用时的返回值\n" +
                    "[-g][--args]     显示调用此命令时使用的参数\n" +
                    "[-t][--invokeAt] 显示何时调用的此命令\n" +
                    "[-i][--interval] 显示执行此命令所花费的时间\n" +
                    "\n" +
                    "提示: -a 参数表示 u r g t i 这几个参数全部选中\n" +
                    "示例，查询最近调用history命令的所有信息\n" +
                    "       his --name history --all\n" +
                    "示例，查询最近10次调用的命令\n" +
                    "       his -n 10\n" +
                    "示例，查询最近5次调用history命令的日期和执行用时\n" +
                    "       his -s history -n 5 -ti\n";
        }

    }

}
