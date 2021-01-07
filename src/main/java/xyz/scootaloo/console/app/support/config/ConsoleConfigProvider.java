package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.ResourceManager;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 配置提供者
 * 启动类必须继承此类，配置可以选择性提供
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:33
 */
public abstract class ConsoleConfigProvider {

    public static final ConsoleConfig DEFAULT_CONFIG = DefaultValueConfigBuilder.defaultConfig();
    private static final Colorful cPrint = ResourceManager.cPrint;

    /**
     * 获取调用此方法的调用者，并实创建调用者的实例
     * @return 调用者的实例
     */
    @Deprecated
    public static ConsoleConfigProvider instance() {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        String invoker = callStack[2].getClassName();
        try {
            Class<?> BOOT_CLAZZ = Class.forName(invoker);
            Object bootObj = BOOT_CLAZZ.newInstance();
            if (!ClassUtils.isExtendForm(bootObj, ConsoleConfigProvider.class))
                cPrint.exit0("启动类没有继承自配置提供者类，无法加载配置");
            return (ConsoleConfigProvider) bootObj;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            cPrint.exit0("解析异常，无法实例化类: " + invoker);
            return null;
        }
    }

    public ConsoleConfig getConfig() {
        ConsoleConfig rsl = register(new DefaultValueConfigBuilder());
        return rsl != null ? rsl : DefaultValueConfigBuilder.defaultConfig();
    }

    public abstract ConsoleConfig register(DefaultValueConfigBuilder builder);

    public static class DefaultValueConfigBuilder {

        // 应用信息
        private String appName = "console";
        private String prompt  = "console> ";
        private String[] exitCmd = {"exit"};
        private boolean printWelcome = true;

        // 开发者配置
        private int maxHistory = 64;
        private boolean printStackTraceOnException = false;
        private List<String> initCommands = new ArrayList<>();
        private Set<Class<?>> factories = new LinkedHashSet<>();

        // 作者信息
        private Author author;

        public DefaultValueConfigBuilder() {
        }

        public DefaultValueConfigBuilder appName(String name) {
            if (appName != null)
                this.appName = name;
            return this;
        }

        public DefaultValueConfigBuilder prompt(String prompt) {
            if (prompt != null) {
                this.prompt = prompt;
            }
            return this;
        }

        public DefaultValueConfigBuilder exitCmd(String cmd) {
            if (cmd != null) {
                this.exitCmd = new String[] {cmd};
            }
            return this;
        }

        public DefaultValueConfigBuilder printWelcome(boolean flag) {
            this.printWelcome = flag;
            return this;
        }

        public DefaultValueConfigBuilder maxHistory(int count) {
            if (count > 0) {
                this.maxHistory = count;
            }
            return this;
        }

        public DefaultValueConfigBuilder printStackTrace(boolean flag) {
            this.printStackTraceOnException = flag;
            return this;
        }

        public DefaultValueConfigBuilder exitCmd(String[] cmd) {
            if (cmd != null) {
                this.exitCmd = cmd;
            }
            return this;
        }

        public StringCommands addInitCommands() {
            return new StringCommands(this);
        }

        public Author editAuthorInfo() {
            this.author = new Author(this);
            return this.author;
        }

        public CommandFactory addCommandFactories() {
            return new CommandFactory(this);
        }

        protected void setInitCommands(StringCommands commands) {
            if (!commands.commandList.isEmpty()) {
                this.initCommands = commands.commandList;
            }
        }

        protected void setCommandFactories(CommandFactory factories) {
            if (!factories.commandFac.isEmpty()) {
                this.factories = factories.commandFac;
            }
        }

        public ConsoleConfig build() {
            return new ConsoleConfig(this);
        }

        private static ConsoleConfig defaultConfig() {
            return new DefaultValueConfigBuilder().build();
        }

    }

    public static class SimpleConfig {

        private final DefaultValueConfigBuilder dvBuilder;

        public SimpleConfig() {
            dvBuilder = new DefaultValueConfigBuilder();
            dvBuilder.printWelcome(false);
        }

        public SimpleConfig addFactory(Class<?> factory, boolean enable) {
            if (enable)
                this.dvBuilder.factories.add(factory);
            return this;
        }

        public SimpleConfig printStackTrace(boolean flag) {
            this.dvBuilder.printStackTrace(false);
            return this;
        }

        public ConsoleConfig build() {
            return dvBuilder.build();
        }

    }

}
