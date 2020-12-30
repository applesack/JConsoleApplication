package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.config.ConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

/**
 * 简易的控制台开发框架
 *
 *
 * 快速学习使用请移步到:
 * {@link xyz.scootaloo.console.app.workspace.QuicklyStart}
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Start extends ConfigProvider {

    public static void main(String[] args) {
        ApplicationRunner.run(instance());
    }

    @Override
    public ConsoleConfig register(DefaultValueConfigBuilder builder) {
        return builder
                .appName("控制台应用示例")
                .appType(AppType.Standard)
                .exitCmd(new String[] {"x", "q", "exit", "e."})
                .prompt("demo> ")
                .basePack("workspace")
                .build();
    }
}
