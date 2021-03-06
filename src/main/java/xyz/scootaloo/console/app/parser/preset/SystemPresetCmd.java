package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Public;
import xyz.scootaloo.console.app.client.Client;
import xyz.scootaloo.console.app.client.ReplacementRecord.KVPair;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.event.AppListenerAdapter;
import xyz.scootaloo.console.app.event.AppListenerProperty;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.parser.Interpreter.MethodActuator;
import xyz.scootaloo.console.app.util.BackstageTaskManager;
import xyz.scootaloo.console.app.util.BackstageTaskManager.BackstageTaskInfo;
import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.VariableManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.scootaloo.console.app.util.VariableManager.msg;
import static xyz.scootaloo.console.app.util.VariableManager.resolvePlaceholders;

/**
 * 系统预设的命令，可以使用 find -t sys 命令查看到
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:43
 */
@Public("所有用户都能使用这些命令")
public final class SystemPresetCmd implements AppListenerAdapter {
    /** singleton */
    protected static final SystemPresetCmd INSTANCE = new SystemPresetCmd();
    private static final Console console = ResourceManager.getConsole();
    private static ConsoleConfig config;

    private static final String version = "v0.3.3";
    public static final String SYS_TAG = "sys";

    // 使用方法返回值做为属性资源

    private SystemPresetCmd() {
    }

    @Cmd(name = "app", tag = SYS_TAG)
    private String application(@Opt('v') boolean ver) {
        if (ver)
            console.println("版本: " + version);
        console.println("https://github.com/applesack/JConsoleApplication.git");
        return version;
    }

    @Cmd(name = "man", tag = SYS_TAG)
    private String help(@Opt(value = 's', fullName = "name") String cmdName) {
        Actuator actuator;
        if (cmdName == null)
            actuator = AssemblyFactory.findActuator("help");
        else
            actuator = AssemblyFactory.findActuator(cmdName);
        String helpDoc = printInfo(actuator);
        console.println(helpDoc);
        return helpDoc;
    }

    @Cmd(name = "tks", tag = SYS_TAG)
    public Set<BackstageTaskInfo> tasks() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Set<BackstageTaskInfo> taskList = Interpreter.getCurrentUser().getResources().getTaskList();
            taskList.forEach(task -> task.showTask(stringBuilder));
            return taskList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Cmd(tag = SYS_TAG, parser = "sub")
    public void task(@Opt(value = 's', fullName = "name", dftVal = "*") String taskName,
                     @Opt(value = 'n', fullName = "size", dftVal = "-1") int size,
                     @Opt(value = 'c', fullName = "clear", dftVal = "*") String clear) {
        Set<BackstageTaskInfo> taskList = Interpreter.getCurrentUser().getResources().getTaskList();
        if (!clear.equals("*")) {
            BackstageTaskManager
                    .clearHistory(taskList, clear.toLowerCase(Locale.ROOT).startsWith("a"));
        } else {
            if (taskName.equals("*")) {
                console.println("没有输入任务名");
                return;
            }
            BackstageTaskManager.showLogs(taskList, taskName, size);
        }
    }

    @Cmd(name = "fd", tag = SYS_TAG)
    private void find(@Opt(value = 's', fullName = "name") String name,
                      @Opt(value = 't', fullName = "tag") String tag) {
        List<MethodActuator> actuatorList = AssemblyFactory.getAllCommands();
        if (name == null && tag == null) {
            actuatorList.forEach(methodActuator -> console.println(methodActuator.getCmdName() + " " + methodActuator.getCmd().name()));
        } else {
            if (name != null) {
                name = name.toLowerCase(Locale.ROOT);
                String finalName = name;
                List<MethodActuator> retainList = actuatorList.stream()
                        .filter(act -> act.getCmdName().equals(finalName) ||
                        act.getCmd().name().equals(finalName)).collect(Collectors.toList());
                if (retainList.isEmpty()) {
                    console.println("没有这个命令的信息: `" + name + "`");
                    return;
                }
                retainList.forEach(this::find);
            } else {
                List<MethodActuator> retainList = actuatorList.stream()
                        .filter(act -> act.getCmd().tag().equals(tag)).collect(Collectors.toList());
                if (retainList.isEmpty()) {
                    console.println("没有这个标签的信息: `" + tag + "`");
                    return;
                }
                retainList.forEach(this::find);
            }
        }
    }

    private void find(MethodActuator methodActuator) {
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        // 输出 标签 类名.方法名(方法参数):返回值
        sb.append('[').append(methodActuator.getCmd().tag()).append(']').append(' ')
                .append(methodActuator.getInstance().getClass().getSimpleName()).append('.')
                .append(ClassUtils.getMethodDescribe(methodActuator.getMethod()));
        console.println(sb);
    }

    @Cmd(name = "lis", tag = SYS_TAG)
    private void listeners() {
        EventPublisher.showAllListeners();
    }

    @Cmd(name = "his", tag = SYS_TAG)
    private List<InvokeInfo> history(@Opt(value = 'n', fullName = "size", dftVal = "-1") int size,
                                     @Opt(value = 's', fullName = "name") String name,
                                     @Opt(value = 'a', fullName = "all" ) boolean isAll,
                                     @Opt(value = 'u', fullName = "success" ) boolean success,
                                     @Opt(value = 'r', fullName = "rtnVal"  ) boolean rtnVal,
                                     @Opt(value = 'g', fullName = "args"    ) boolean args,
                                     @Opt(value = 't', fullName = "invokeAt") boolean invokeAt,
                                     @Opt(value = 'i', fullName = "interval") boolean interval) {
        return Interpreter.getCurrentUser().getResources().getHistory()
                .select(name, size, isAll, success, rtnVal, args, invokeAt, interval);
    }

    @Cmd(tag = SYS_TAG)
    private int sleep(@Opt('m') int millis) throws InterruptedException {
        if (millis >= 0)
        Thread.sleep(millis);
        return millis;
    }

    @Cmd(name = "cls", tag = SYS_TAG)
    private void clear() throws IOException, InterruptedException {
        // 新建一个 ProcessBuilder，其要执行的命令是 cmd.exe，参数是 /c 和 cls
        new ProcessBuilder("cmd", "/c", "cls")
                // 将 ProcessBuilder 对象的输出管道和 Java 的进程进行关联，这个函数的返回值也是一个
                // ProcessBuilder
                .inheritIO()
                // 开始执行 ProcessBuilder 中的命令
                .start()
                // 等待 ProcessBuilder 中的清屏命令执行完毕
                // 如果不等待则会出现清屏代码后面的输出被清掉的情况
                .waitFor(); // 清屏命令

    }

    private static String getClearCommand() {
        Properties prop = System.getProperties();
        String os = prop.getProperty("os.name");
        if (os != null && os.toLowerCase().contains("linux")) {
            return "clear";
        } else {
            return "cls";
        }
    }

    private String printInfo(Actuator actuator) {
        if (actuator instanceof MethodActuator) {
            return  ((MethodActuator) actuator).getHelpInfo();
        } else {
            return "系统中没有这个命令.";
        }
    }

    @Cmd(tag = SYS_TAG, parser = "sub")
    private boolean set(@Opt(value = 'k', fullName = "key") String key,
                     @Opt(value = 'v', fullName = "value") String value) {
        if (!config.isEnableVariableFunction()) {
            console.println(msg);
            return false;
        }
        Map<String, Object> variablePool = Interpreter.getCurrentUser().getResources().getVariablePool();
        if (key == null) {
            console.println("未选中键");
            return false;
        }
        if (key.startsWith(".")) {
            VariableManager.set(".", ".", variablePool);
            return true;
        }
        return VariableManager.set(key, value, variablePool);
    }

    @Cmd(tag = SYS_TAG)
    private Object get(@Opt(value = 'k', fullName = "key") String key) {
        Map<String, Object> variablePool = Interpreter.getCurrentUser().getResources().getVariablePool();
        Optional<Object> val = VariableManager.get(key, variablePool);
        if (!val.isPresent()) {
            console.println("没有这个键的信息");
            return null;
        } else {
            console.println(val.get());
            return val.get();
        }
    }

    @Cmd(tag = SYS_TAG)
    private Map<String, Object> keys() {
        Map<String, Object> kvMap = Interpreter.getCurrentUser()
                .getResources()
                .getVariablePool();
        kvMap.forEach((k, v) -> console.println("[" + k + "]: " + v));
        return kvMap;
    }

    @Cmd(tag = SYS_TAG)
    private void echo(@Opt(value = 'v', fullName = "value") String val) {
        Interpreter.getCurrentUser().getResources()
                .getReplacementRecord().getRecords()
                    .forEach(this::echoPrint);
    }

    private void echoPrint(KVPair kv) {
        console.println(kv.key + " [type: " + kv.value.getClass()
                .getSimpleName() + "] " + kv.value);
    }

    // ---------------------------------监听器----------------------------------------

    @Override
    public String getName() {
        return "sys";
    }

    @Override
    public void config(AppListenerProperty interestedInEvents) {
        // 指定几个感兴趣的事件，并指定优先级为 -1
        interestedInEvents
                .onInput(-1)
                .onInputResolved(-1)
                .onAppStarted(-1)
                .onResolveInput(-1);
    }

    @Override
    public void onAppStarted(ConsoleConfig conf) {
        config = conf;
    }

    @Override
    public void onResolveInput(String cmdName, List<String> cmdItems) {
        Client.Resources resources = Interpreter.getCurrentUser().getResources();
        resources.getReplacementRecord().refresh();
        if (cmdItems != null) {
            for (int i = 0; i<cmdItems.size(); i++) {
                cmdItems.set(i, resolvePlaceholders(cmdItems.get(i),
                        resources.getReplacementRecord(), resources.getVariablePool()));
            }
        }
    }

    @Override
    public void onInputResolved(String cmdName, InvokeInfo info) {
        if (info != null) {
            // 记录命令行执行信息
            Interpreter.getCurrentUser().getResources().getHistory().add(info);
        }
    }

    @Override
    public String info() {
        return "系统自带的监听器，将命令参数中的占位符替换成函数的返回值，另外记录执行过的命令信息";
    }

    //----------------------------------------------------------------------------------

    //--------------------------------------------------------------------------------

    // 对于系统预置命令的描述
    public static final class Help implements HelpDoc {
        /** singleton */
        protected static final Help INSTANCE = new Help();

        public String _app() {
            return "应用信息\n" +
                    "app [-v/--version]\n" +
                    "-v/--version 查看应用的版本信息\n";
        }

        public String _help() {
            return "帮助信息\n" +
                    "help [-s/--name] \n" +
                    "查询某命令的用法\n" +
                    "示例，查询history这个命令的用法: \n" +
                    "       help -s history\n" +
                    "提示: 这里的`[-s]`和`[--name]`在同一行，表示同一个参数的不同写法，一个是简写一个是全称\n" +
                    "help -s help 等于 help --name help\n";
        }

        public String _find() {
            return "查找某命令的信息\n" +
                    "find [-s/--name] [-t/--tag]\n" +
                    "-s/--name 根据名称查找命令\n" +
                    "-t/--tag  根据标签查找命令\n" +
                    "示例，查找history命令的信息\n" +
                    "       find -s history\n" +
                    "示例，查找sys标签的命令\n" +
                    "       find -t sys\n";
        }

        public String _cls() {
            return "清空控制台\n" +
                    "无需参数\n" +
                    "这个功能目前只在windows下且是cmd环境才有用\n";
        }

        public String _listeners() {
            return "查看当前正在启用的监听器\n" +
                    "无需参数\n";
        }

        public String _sleep() {
            return "休眠当前程序一段时间\n" +
                    "sleep <-m <millisecond>>\n" +
                    "-m 单位毫秒\n" +
                    "示例，休眠当前程序100毫秒\n" +
                    "       sleep -m 100\n" +
                    "或者\n" +
                    "       sleep 100\n";
        }

        public String _history() {
            return "查询历史记录\n" +
                    "his [-n/--size] [-s/--name] [-a/--all] [-u/--success] [-r/--rtnVal] [-g/--args] [-t/--invokeAt] [-i/--interval]\n" +
                    "-n/--size     需要展示历史记录的数量，显示最近的记录\n" +
                    "-s/--name     查找指定的命令调用记录\n" +
                    "-a/--all      显示命令调用的所有信息\n" +
                    "-u/--success  显示命令调用是否成功\n" +
                    "-r/--rtnVal   显示命令调用时的返回值\n" +
                    "-g/--args     显示调用此命令时使用的参数\n" +
                    "-t/--invokeAt 显示何时调用的此命令\n" +
                    "-i/--interval 显示执行此命令所花费的时间\n" +
                    "\n" +
                    "提示: -a 参数表示 u r g t i 这几个参数全部选中\n" +
                    "示例，查询最近调用history命令的所有信息\n" +
                    "       his --name history --all\n" +
                    "示例，查询最近10次调用的命令\n" +
                    "       his -n 10\n" +
                    "示例，查询最近5次调用history命令的日期和执行用时\n" +
                    "       his -s history -n 5 -ti\n";
        }

        public String _set() {
            return "设置变量\n" +
                    "set <key> <value>\n" +
                    "向系统中放置一个键值对\n" +
                    "\n" +
                    "可以用命令方法的返回值做为key的value\n" +
                    "set <key>\n" +
                    "a command which has return value\n" +
                    "\n" +
                    "清除某个key\n" +
                    "set <key> .\n" +
                    "\n" +
                    "清除所有key\n" +
                    "set .\n";
        }

        public String _get() {
            return "获取键的值，并输出\n" +
                    "get <key>\n";
        }

        public String _keys() {
            return "无需参数，显示所有的键值对\n";
        }

        public String _echo() {
            return "echo <${val}>\n" +
                    "接收一个参数，显示变量的实际值，也可以用于查看变量对象的属性\n" +
                    "示例，这里假设这些变量是存在的\n" +
                    "   echo ${name}\n" +
                    "也可以同时查看多个变量的状态\n" +
                    "   echo ${name} ${age} ${height}\n" +
                    "查看变量的某属性，假定stu变量是一个Student类的实例，它具有age这个域，则可以这样做\n" +
                    "   echo ${stu.age}\n" +
                    "注意: echo 命令有返回值，可以做为变量\n";
        }

        public String _tasks() {
            return "无需参数\n" +
                    "输出任务管理器中的任务信息\n";
        }

        public String _task() {
            return "用法1\n" +
                    "   task <taskName> <count>\n" +
                    "   例如: task test 7\n" +
                    "   这是查看名为test的任务最近输出的7条内容\n" +
                    "   如果不指定count，则默认为查看全部输出\n" +
                    "用法2\n" +
                    "   task clear all\n" +
                    "   清除后台全部任务\n" +
                    "   task clear\n" +
                    "   清除后台以完成的任务\n";
        }

    }

}
