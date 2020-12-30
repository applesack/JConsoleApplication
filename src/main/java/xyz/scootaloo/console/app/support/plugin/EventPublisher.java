package xyz.scootaloo.console.app.support.plugin;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:44
 */
public class EventPublisher {

    private static final Colorful cPrint = Colorful.instance;
    private static final Map<String, ConsolePlugin> PLUGIN_MAP = new LinkedHashMap<>();
    private static final Map<String, ConsolePlugin> CANDIDATE_MAP = new HashMap<>();

    private EventPublisher() {
    }

    public static void loadPlugin(ConsolePlugin plugin) {
        String  pluginName = plugin.getName().toLowerCase(Locale.ROOT);
        PLUGIN_MAP.put(pluginName, plugin);
    }

    public static void showAllPlugins() {
        for (Map.Entry<String, ConsolePlugin> pluginEntry : PLUGIN_MAP.entrySet()) {
            cPrint.println("[" +pluginEntry.getKey() + "] " + pluginEntry.getValue().info());
        }
    }

    public static void disablePlugin(String pluginName) {
        pluginName = pluginName.toLowerCase(Locale.ROOT);
        if (PLUGIN_MAP.containsKey(pluginName)) {
            ConsolePlugin plugin = PLUGIN_MAP.remove(pluginName);
            CANDIDATE_MAP.put(pluginName, plugin);
            System.out.println("删除成功: [" + pluginName + "]");
        } else {
            System.out.println("删除失败: 系统中没有这个插件[" + pluginName + "]");
        }
    }

    public static void enablePlugin(String pluginName) {
        pluginName = pluginName.toLowerCase(Locale.ROOT);
        if (PLUGIN_MAP.containsKey(pluginName)) {
            System.out.println("系统中已存在此插件: [" + pluginName + "]");
            return;
        }
        if (CANDIDATE_MAP.containsKey(pluginName)) {
            ConsolePlugin plugin = CANDIDATE_MAP.remove(pluginName);
            PLUGIN_MAP.put(pluginName, plugin);
            System.out.println("启用成功: [" + pluginName + "]");
        } else {
            System.out.println("启用失败: 系统中没有这个插件[" + pluginName + "]");
        }
    }

    public static void onAppStarted(ConsoleConfig config) {
        PLUGIN_MAP.values().stream()
                .filter(plugin -> plugin.accept(Moment.OnAppStarted))
                .forEach(plugin -> plugin.onAppStarted(config));
    }

    public static String onInput(String cmdline) {
        List<ConsolePlugin> pluginList = PLUGIN_MAP.values().stream()
                .filter(plugin -> plugin.accept(Moment.OnInput)).collect(Collectors.toList());
        for (ConsolePlugin plugin : pluginList) {
            cmdline = plugin.onInput(cmdline);
        }
        return cmdline;
    }

    public static void onResolveInput(String cmdName, List<String> cmdItems) {
        PLUGIN_MAP.values().stream()
                .filter(plugin -> plugin.accept(Moment.OnResolveInput))
                .forEach(plugin -> plugin.onResolveInput(cmdName, cmdItems));
    }

    public static void onInputResolved(String cmdName, Object rtnVal) {
        PLUGIN_MAP.values().stream()
                .filter(plugin -> plugin.accept(Moment.OnInputResolved))
                .forEach(plugin -> plugin.onInputResolved(cmdName, rtnVal));
    }

}
