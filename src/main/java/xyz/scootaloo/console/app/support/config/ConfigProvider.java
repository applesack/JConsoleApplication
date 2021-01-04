package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.component.Author;
import xyz.scootaloo.console.app.support.component.ResourceManager;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * 配置提供者
 * 启动类必须继承此类，配置可以选择性提供
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:33
 */
public abstract class ConfigProvider {
    private static final Colorful cPrint = ResourceManager.cPrint;
    private static Class<?> BOOT_CLAZZ;

    /**
     * 获取调用此方法的调用者，并实创建调用者的实例
     * @return 调用者的实例
     */
    public static ConfigProvider instance() {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        String invoker = callStack[2].getClassName();
        try {
            BOOT_CLAZZ = Class.forName(invoker);
            Object bootObj = BOOT_CLAZZ.newInstance();
            if (!ClassUtils.isExtendForm(bootObj, ConfigProvider.class))
                cPrint.exit0("启动类没有继承自配置提供者类，无法加载配置");
            return (ConfigProvider) bootObj;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            cPrint.exit0("解析异常，无法实例化类: " + invoker);
            return null;
        }
    }

    public ConsoleConfig getConfig() {
        if (BOOT_CLAZZ == null)
            cPrint.exit0("请使用 instance() 方法做为ConsoleApplication.run()的参数");
        ConsoleConfig rsl = register(new DefaultValueConfigBuilder(BOOT_CLAZZ));
        return rsl != null ? rsl : DefaultValueConfigBuilder.defaultConfig(BOOT_CLAZZ);
    }

    public abstract ConsoleConfig register(DefaultValueConfigBuilder builder);

    public String[] getInitCommands() {
        return null;
    }

    public static class DefaultValueConfigBuilder {
        private final Class<?> bootClazz;

        // 应用信息
        private AppType appType = AppType.Standard;
        private String appName;
        private String prompt  = "console> ";
        private String[] exitCmd = {"exit"};
        private boolean printWelcome = true;
        private String basePack;

        // 开发者配置
        private int maxHistory = 64;
        private boolean printStackTraceOnException = false;

        // 作者信息
        private Author author;

        public DefaultValueConfigBuilder(Class<?> bootClazz) {
            this.bootClazz = bootClazz;
            appName = getAppName(bootClazz);
            basePack = getBasePack(bootClazz);
        }

        public DefaultValueConfigBuilder appName(String name) {
            if (appName != null)
                this.appName = name;
            return this;
        }

        public DefaultValueConfigBuilder appType(AppType type) {
            if (type != null) {
                this.appType = type;
            }
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

        public DefaultValueConfigBuilder basePack(String pack) {
            if (pack != null) {
                if (pack.indexOf('.') == -1) {
                    this.basePack = getBasePack(bootClazz) + "." + pack;
                } else {
                    this.basePack = pack;
                }
            }
            return this;
        }

        public Author editAuthorInfo() {
            this.author = new Author(this);
            return this.author;
        }

        public ConsoleConfig build() {
            return new ConsoleConfig(this);
        }

        private static String getBasePack(Class<?> bootClazz) {
            return bootClazz.getPackage().getName();
        }

        private static String getAppName(Class<?> bootClazz) {
            return bootClazz.getSimpleName();
        }

        private static ConsoleConfig defaultConfig(Class<?> bootClazz) {
            return new DefaultValueConfigBuilder(bootClazz).build();
        }
    }

}
