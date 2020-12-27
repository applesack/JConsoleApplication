package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:33
 */
public abstract class ApplicationConfig extends Colorful {

    private static Class<?> BOOT_CLAZZ;

    /**
     * 获取调用此方法的调用者，并实创建调用者的实例
     * @return 调用者的实例
     */
    public static ApplicationConfig instance() {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        String invoker = callStack[2].getClassName();
        try {
            BOOT_CLAZZ = Class.forName(invoker);
            Object bootObj = BOOT_CLAZZ.newInstance();
            if (!ClassUtils.isExtendForm(bootObj, ApplicationConfig.class))
                exit0("启动类没有继承自配置类，无法加载配置");
            return (ApplicationConfig) bootObj;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            exit0(grey("解析异常，无法实例化类: ") + red(invoker));
            return null;
        }
    }

    public ConsoleConfig getConfig() {
        if (BOOT_CLAZZ == null)
            exit0("请使用 instance() 方法做为ConsoleApplication.run()的参数");
        ConsoleConfig rsl = register(new DefaultValueConfigBuilder(BOOT_CLAZZ));
        return rsl != null ? rsl : DefaultValueConfigBuilder.defaultConfig(BOOT_CLAZZ);
    }

    public abstract ConsoleConfig register(DefaultValueConfigBuilder configBuilder);

    public static class DefaultValueConfigBuilder {

        private AppType appType = AppType.Standard;
        private String  appName;
        private String  prompt  = "console> ";
        private String  exitCmd = "exit";

        public DefaultValueConfigBuilder(Class<?> bootClazz) {
            appName = getAppName(bootClazz);
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
                this.exitCmd = cmd;
            }
            return this;
        }

        public ConsoleConfig build() {
            return new ConsoleConfig(this);
        }

        private static String getAppName(Class<?> bootClazz) {
            return bootClazz.getSimpleName();
        }

        private static ConsoleConfig defaultConfig(Class<?> bootClazz) {
            return new DefaultValueConfigBuilder(bootClazz).build();
        }
    }

}
