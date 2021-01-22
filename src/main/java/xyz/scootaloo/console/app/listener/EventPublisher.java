package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.AppListener;
import xyz.scootaloo.console.app.common.Colorful;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.anno.Moment;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统事件发布器，在运行的不同节点发布事件
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:44
 */
public class EventPublisher {

    private static final Colorful cPrint = ResourceManager.getColorfulPrinter();
    private static final Map<String, AppListener> LISTENER_MAP = new LinkedHashMap<>();
    private static final Map<String, AppListener> CANDIDATE_MAP = new HashMap<>();

    private EventPublisher() {
    }

    public static void loadListener(AppListener listener) {
        String listenerName = listener.getName().toLowerCase(Locale.ROOT);
        LISTENER_MAP.put(listenerName, listener);
    }

    public static void showAllListeners() {
        for (Map.Entry<String, AppListener> listenerEntry : LISTENER_MAP.entrySet()) {
            cPrint.println("[" +listenerEntry.getKey() + "] " + listenerEntry.getValue().info());
        }
    }

    public static void disableListener(String listenerName) {
        listenerName = listenerName.toLowerCase(Locale.ROOT);
        if (LISTENER_MAP.containsKey(listenerName)) {
            AppListener listener = LISTENER_MAP.remove(listenerName);
            CANDIDATE_MAP.put(listenerName, listener);
            System.out.println("删除成功: [" + listenerName + "]");
        } else {
            System.out.println("删除失败: 系统中没有这个监听器[" + listenerName + "]");
        }
    }

    public static void enableListener(String listenerName) {
        listenerName = listenerName.toLowerCase(Locale.ROOT);
        if (LISTENER_MAP.containsKey(listenerName)) {
            System.out.println("系统中已存在此监听器: [" + listenerName + "]");
            return;
        }
        if (CANDIDATE_MAP.containsKey(listenerName)) {
            AppListener listener = CANDIDATE_MAP.remove(listenerName);
            LISTENER_MAP.put(listenerName, listener);
            System.out.println("启用成功: [" + listenerName + "]");
        } else {
            System.out.println("启用失败: 系统中没有这个监听器[" + listenerName + "]");
        }
    }

    //----------------------------------事件发布------------------------------------------

    // 系统起步时
    public static void onAppStarted(ConsoleConfig config) {
        LISTENER_MAP.values().stream()
                .filter(listener -> listener.accept(Moment.OnAppStarted))
                .forEach(listener -> listener.onAppStarted(config));
    }

    // 获取控制台输入时
    public static String onInput(String cmdline) {
        List<AppListener> listenerList = LISTENER_MAP.values().stream()
                .filter(listener -> listener.accept(Moment.OnInput)).collect(Collectors.toList());
        for (AppListener listener : listenerList) {
            cmdline = listener.onInput(cmdline);
        }
        return cmdline;
    }

    // 解析输入前
    public static void onResolveInput(String cmdName, List<String> cmdItems) {
        LISTENER_MAP.values().stream()
                .filter(listener -> listener.accept(Moment.OnResolveInput))
                .forEach(listener -> listener.onResolveInput(cmdName, cmdItems));
    }

    // 解析输入后
    public static void onInputResolved(String cmdName, InvokeInfo info) {
        LISTENER_MAP.values().stream()
                .filter(listener -> listener.accept(Moment.OnInputResolved))
                .forEach(listener -> listener.onInputResolved(cmdName, info));
    }

}
