package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.config.ConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

/**
 * 基础的控制台开发框架
 * 简化开发过程，自动装配命令，解析命令参数
 * 支持可选参数和必选参数
 *
 * 快速学习使用请移步到:
 * {@link xyz.scootaloo.console.app.workspace.QuicklyStart}
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Start extends ConfigProvider {

    /**
     * 使用过程：
     * 1. 在此类的 {@link #register(DefaultValueConfigBuilder)} 方法中设置自己需要的配置。
     * 2. 在workspace目录下进行开发。
     * 3. 回到此类运行main方法，系统启动。
     *
     * @param args ignore
     */
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
