package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ConsoleMessage;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.listener.AppListenerProperty.EventProperty;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.*;

/**
 * 系统事件发布器，在运行的不同节点发布事件
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:44
 */
public final class EventPublisher {
    /** resources */
    private static final Console console = ResourceManager.getConsole();
    private static final Map<Occasion, List<ListenerWrapper>> LISTENERS = new HashMap<>();
    private static final List<AppListener> WORKING_LISTENERS = new ArrayList<>();
    private static volatile boolean hasEnable;

    // private constructor
    private EventPublisher() {
    }

    /**
     * 注册一个监听器到系统
     * @param listener 要注册进来的监听器
     */
    public static void regListener(AppListener listener) {
        hasEnable = false;

        ListenerWrapper wrapper = new ListenerWrapper(listener);
        EventProperty eventProperty = wrapper.appListenerProperty.get();
        if (eventProperty.onAppStarted.isInterestedIn())
            putToCollection(Occasion.OnAppStarted, wrapper);
        if (eventProperty.onInput.isInterestedIn())
            putToCollection(Occasion.OnInput, wrapper);
        if (eventProperty.onResolveInput.isInterestedIn())
            putToCollection(Occasion.OnResolveInput, wrapper);
        if (eventProperty.onInputResolved.isInterestedIn())
            putToCollection(Occasion.OnInputResolved, wrapper);
        if (eventProperty.onMessage.isInterestedIn())
            putToCollection(Occasion.OnMessage, wrapper);

        if (hasEnable) {
            WORKING_LISTENERS.add(listener);
            sortListeners();
        }
    }

    /**
     * 查看系统中正在工作的监听器
     */
    public static void showAllListeners() {
        WORKING_LISTENERS.forEach(
                 listener -> console.println("[" + listener.getName() + "] " + listener.info())
        );
    }

    private static void putToCollection(Occasion key, ListenerWrapper value) {
        hasEnable = true;
        Optional<List<ListenerWrapper>> optional = Optional.ofNullable(LISTENERS.get(key));
        if (!optional.isPresent()) {
            List<ListenerWrapper> listeners = new ArrayList<>();
            listeners.add(value);
            LISTENERS.put(key, listeners);
        } else {
            optional.get().add(value);
        }
    }

    private static void sortListeners() {
        LISTENERS.forEach((occasion, listenerWrappers) -> {
            if (!listenerWrappers.isEmpty()) {
                listenerWrappers.sort(Comparator.comparingInt(wrap ->
                        wrap.getProperty().get(occasion).priority()));
            }
        });
    }

    //----------------------------------事件发布------------------------------------------

    // 系统起步时
    public static void onAppStarted(ConsoleConfig config) {
        Optional.ofNullable(LISTENERS.get(Occasion.OnAppStarted))
                .ifPresent(listenerWrappers ->
                        listenerWrappers.forEach(wrapper ->
                                wrapper.onAppStarted(config)));
    }

    // 获取控制台输入时
    public static String onInput(String cmdline) {
        final String[] modifiedCmdline = {cmdline};
        Optional.ofNullable(LISTENERS.get(Occasion.OnInput))
                .ifPresent(listenerWrappers -> {
                    for (AppListener listener : listenerWrappers)
                        modifiedCmdline[0] = listener.onInput(modifiedCmdline[0]);
                });
        return modifiedCmdline[0];
    }

    // 解析输入前
    public static void onResolveInput(String cmdName, List<String> cmdItems) {
        Optional.ofNullable(LISTENERS.get(Occasion.OnResolveInput))
                .ifPresent(listenerWrappers ->
                        listenerWrappers.forEach(wrapper ->
                                wrapper.onResolveInput(cmdName, cmdItems)));
    }

    // 解析输入后
    public static void onInputResolved(String cmdName, InvokeInfo info) {
        Optional.ofNullable(LISTENERS.get(Occasion.OnInputResolved))
                .ifPresent(listenerWrappers ->
                        listenerWrappers.forEach(wrapper ->
                                wrapper.onInputResolved(cmdName, info)));
    }

    // 产生消息时
    public static void onMessage(ConsoleMessage message) {
        Optional.ofNullable(LISTENERS.get(Occasion.OnMessage))
                .ifPresent(listenerWrappers ->
                        listenerWrappers.forEach(wrapper ->
                                wrapper.onMessage(message)));
    }

    //--------------------------------------------------------------------------

    private static class ListenerWrapper implements AppListener {
        private final AppListener impl;
        private final AppListenerProperty appListenerProperty;

        public ListenerWrapper(AppListener impl) {
            this.impl = impl;
            appListenerProperty = new AppListenerProperty();
            config(appListenerProperty);
        }

        public EventProperty getProperty() {
            return this.appListenerProperty.get();
        }

        @Override
        public boolean enable() {
            return impl.enable();
        }

        @Override
        public String getName() {
            return impl.getName();
        }

        @Override
        public void config(AppListenerProperty interested) {
            impl.config(interested);
        }

        @Override
        public void onAppStarted(ConsoleConfig config) {
            impl.onAppStarted(config);
        }

        @Override
        public String onInput(String cmdline) {
            return impl.onInput(cmdline);
        }

        @Override
        public void onResolveInput(String cmdName, List<String> cmdItems) {
            impl.onResolveInput(cmdName, cmdItems);
        }

        @Override
        public void onInputResolved(String cmdName, InvokeInfo info) {
            impl.onInputResolved(cmdName, info);
        }

        @Override
        public void onMessage(ConsoleMessage message) {
            impl.onMessage(message);
        }

    }

}
