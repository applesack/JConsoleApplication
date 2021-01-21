package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.common.Console;
import xyz.scootaloo.console.app.support.common.ResourceManager;
import xyz.scootaloo.console.app.support.parser.ParameterParser;
import xyz.scootaloo.console.app.support.utils.ClassUtils;
import xyz.scootaloo.console.app.support.utils.YmlConfReader;

import java.util.*;
import java.util.function.Supplier;

/**
 * 配置提供者
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:33
 */
public abstract class ConsoleConfigProvider {

    private static final Console printer = ResourceManager.getConsole();

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
                printer.exit0("启动类没有继承自配置提供者类，无法加载配置");
            return (ConsoleConfigProvider) bootObj;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            printer.exit0("解析异常，无法实例化类: " + invoker);
            return null;
        }
    }

    // 所有域都有默认值的默认值构建者
    public static class DefaultValueConfigBuilder {

        // 应用信息
        private String appName = "console";
        private String prompt  = "console> ";
        private String[] exitCmd = {"exit"};
        private boolean printWelcome = true;
        private String configFileName = YmlConfReader.DFT_FILENAME;

        // 开发者配置
        private int maxHistory = 64;
        private boolean printStackTraceOnException = false;
        private List<String> initCommands = new ArrayList<>();
        private Set<Supplier<Object>> factories = new LinkedHashSet<>();
        private boolean enableVariableFunction = true;

        // 作者信息
        private Author author = new Author(this);

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

        public DefaultValueConfigBuilder setConfigFile(String filename) {
            this.configFileName = filename;
            return this;
        }

        public String getConfigFileName() {
            return this.configFileName;
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

        protected void setParserMap(CustomizeParser customizeParser) {
            for (Map.Entry<String, ParameterParser> entry : customizeParser.parserMap.entrySet()) {
                this.factories.add(entry::getValue);
            }
        }

        public DefaultValueConfigBuilder enableVariableFunction(boolean flag) {
            this.enableVariableFunction = flag;
            return this;
        }

        public DefaultValueConfigBuilder addHelpDoc(Object o) {
            if (o != null)
                this.factories.add(() -> o);
            return this;
        }

        public StringCommands addInitCommands() {
            return new StringCommands(this);
        }

        public Author editAuthorInfo() {
            this.author = new Author(this);
            return this.author;
        }

        public FactoryCollector addCommandFactories() {
            return new FactoryCollector(this);
        }

        public CustomizeParser addParameterParser() {
            return new CustomizeParser(this);
        }

        protected void setInitCommands(StringCommands commands) {
            if (!commands.commandList.isEmpty()) {
                this.initCommands = commands.commandList;
            }
        }

        protected void setCommandFactories(FactoryCollector factories) {
            if (!factories.commandFac.isEmpty()) {
                this.factories = factories.commandFac;
            }
        }

        public ConsoleConfig build() {
            YmlConfReader.loadConf(this);
            return new ConsoleConfig(this);
        }

        private static ConsoleConfig defaultConfig() {
            return new DefaultValueConfigBuilder().build();
        }

    }

    // 简单配置的配置类
    public static class SimpleConfigBuilder {

        private final DefaultValueConfigBuilder dvBuilder;

        public SimpleConfigBuilder() {
            dvBuilder = new DefaultValueConfigBuilder();
            dvBuilder.printWelcome(false);
        }

        public FactoryCollector addFactories() {
            return new FactoryCollector(this.dvBuilder);
        }

    }

}
