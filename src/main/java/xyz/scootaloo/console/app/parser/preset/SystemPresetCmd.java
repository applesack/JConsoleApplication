package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Public;
import xyz.scootaloo.console.app.client.Client;
import xyz.scootaloo.console.app.client.ReplacementRecord.KVPair;
import xyz.scootaloo.console.app.client.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.event.AppListenerAdapter;
import xyz.scootaloo.console.app.event.AppListenerProperty;
import xyz.scootaloo.console.app.event.EventPublisher;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.parser.Interpreter.MethodActuator;
import xyz.scootaloo.console.app.support.BackstageTaskManager;
import xyz.scootaloo.console.app.support.BackstageTaskManager.BackstageTaskInfo;
import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.support.VariableManager;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static xyz.scootaloo.console.app.support.VariableManager.msg;
import static xyz.scootaloo.console.app.support.VariableManager.resolvePlaceholders;

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

    private static final String version = "v0.3.4";
    public static final String SYS_TAG = "sys";

    // 使用方法返回值做为属性资源

    private SystemPresetCmd() {
    }

    @Cmd(name = "app", tag = SYS_TAG, comment = "查看应用信息")
    private String application(@Opt('v') boolean ver) {
        if (ver)
            console.println("版本: " + version);
        else
            console.println("https://github.com/applesack/JConsoleApplication.git");
        return version;
    }

    @Cmd(name = "man", tag = SYS_TAG, comment = "查看帮助信息")
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

    @Cmd(name = "tks", tag = SYS_TAG, comment = "查看已提交的后台任务")
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

    @Cmd(tag = SYS_TAG, parser = "sub", comment = "查看某个后台任务的详细信息")
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

    @Cmd(name = "fd", tag = SYS_TAG, comment = "查看某个注册到系统的可调用的java方法")
    private void find(@Opt(value = 's', fullName = "name") String name,
                      @Opt(value = 't', fullName = "tag") String tag) {
        List<MethodActuator> actuatorList = AssemblyFactory.getAllCommands();
        if (name == null && tag == null) {
            actuatorList.forEach(methodActuator -> console.println(methodActuator.getCmdName() +
                    " " + methodActuator.getCmd().name()));
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

    @Cmd(name = "lis", tag = SYS_TAG, comment = "查看系统中的监听器")
    private void listeners() {
        EventPublisher.showAllListeners();
    }

    @Cmd(name = "his", tag = SYS_TAG, comment = "查看历史记录")
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

    @Cmd(tag = SYS_TAG, comment = "休眠当前线程一段时间")
    private int sleep(@Opt('m') int millis) throws InterruptedException {
        if (millis >= 0)
        Thread.sleep(millis);
        return millis;
    }

    @Cmd(name = "cls", tag = SYS_TAG, comment = "清空控制台")
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

    @Cmd(tag = SYS_TAG, parser = "sub", comment = "设置变量")
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

    @Cmd(tag = SYS_TAG, comment = "查看变量")
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

    @Cmd(tag = SYS_TAG, comment = "查看所有变量")
    private Map<String, Object> keys() {
        Map<String, Object> kvMap = Interpreter.getCurrentUser()
                .getResources()
                .getVariablePool();
        kvMap.forEach((k, v) -> console.println("[" + k + "]: " + v));
        return kvMap;
    }

    @Cmd(tag = SYS_TAG, comment = "查看变量的实际类型和值")
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
    public void beforeResolveInput(String cmdName, List<String> cmdItems) {
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
    public static final class SystemCommandHelp implements HelpDoc {
        /** singleton */
        protected static final SystemCommandHelp INSTANCE = new SystemCommandHelp();

        public String _app() {
            return "应用信息\n" +
                    "    -v, --version          查看应用的版本信息\n";
        }

        public String _help() {
            return "帮助信息\n" +
                    "    -s, --name <cmdName>   查询某命令的用法\n\n" +
                    "示例，查询history这个命令的用法: \n" +
                    "    help -s history\n" +
                    "或者   \n" +
                    "    help --name history\n";
        }

        public String _find() {
            return "查找某命令的信息\n" +
                    "    <>                     不带参数, 查看所有的命令\n" +
                    "    -s, --name <cmdName>   查看某个命令对应的java方法信息\n" +
                    "    -t, --tag <tag>        查看标记为某个tag的可调用命令\n";
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
                    "    [-m] <count>           休眠当前线程count毫秒\n\n" +
                    "示例, 休眠200毫秒\n" +
                    "     sleep -m 200\n" +
                    "或者\n" +
                    "     sleep 200\n";
        }

        public String _history() {
            return "查询历史记录\n" +
                    "    -n, --size             需要展示历史记录的数量，显示最近的记录\n" +
                    "    -s, --name             查找指定的命令调用记录\n" +
                    "    -a, --all              显示命令调用的所有信息\n" +
                    "    -u, --success          显示命令调用是否成功\n" +
                    "    -r, --rtnVal           显示命令调用时的返回值\n" +
                    "    -g, --args             显示调用此命令时使用的参数\n" +
                    "    -t, --invokeAt         显示何时调用的此命令\n" +
                    "    -i, --interval         显示执行此命令所花费的时间\n";
        }

        public String _set() {
            return "设置变量\n" +
                    "    <key> <value>          放置一对键值对, key是键, 值是value\n" +
                    "    <key> .                清除指定key的记录\n" +
                    "    .                      清除所有key记录\n";
        }

        public String _get() {
            return "获取键的值，并输出\n" +
                    "    <key>                  输出指定key的value信息\n";
        }

        public String _keys() {
            return "无需参数，显示所有的键值对\n";
        }

        public String _echo() {
            return "echo <${val}>\n" +
                    "接收一个参数，显示变量的实际值，也可以用于查看变量对象的属性\n" +
                    "示例，这里假设这些变量是存在的\n" +
                    "    echo ${name}\n" +
                    "也可以同时查看多个变量的状态\n" +
                    "    echo ${name} ${age} ${height}\n" +
                    "查看变量的某属性，假定stu变量是一个Student类的实例，它具有age这个域，则可以这样做\n" +
                    "    echo ${stu.age}\n" +
                    "注意: echo 命令有返回值，可以做为变量\n";
        }

        public String _tasks() {
            return "无需参数\n" +
                    "输出任务管理器中的任务信息\n";
        }

        public String _task() {
            return "查看任务信息:\n" +
                    "   <taskName> [count]      显示任务名为taskName的任务最近输出的count条记录, 假如不指定count将输出所有记录\n" +
                    "   clear all               清除后台记录的所有任务\n" +
                    "   clear                   清除后台已经完成的任务\n";
        }

    }

}
