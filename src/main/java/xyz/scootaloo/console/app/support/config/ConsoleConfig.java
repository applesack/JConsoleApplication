package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:37
 */
public class ConsoleConfig {
    // 应用类型
    private AppType appType;

    // 应用信息
    private String appName; // 应用名称
    private String prompt;  // 控制台提示
    private String exitCmd; // 退出时使用的命令

    // 扫描的基础包路径


    public ConsoleConfig(ApplicationConfig.DefaultValueConfigBuilder builder) {
        ClassUtils.copyProperties(builder, this);
    }

    @Override
    public String toString() {
        return "ConsoleConfig{" +
                "appType=" + appType +
                ", appName='" + appName + '\'' +
                ", prompt='" + prompt + '\'' +
                ", exitCmd='" + exitCmd + '\'' +
                '}';
    }

    public AppType getAppType() {
        return appType;
    }

    public String getAppName() {
        return appName;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getExitCmd() {
        return exitCmd;
    }

}
