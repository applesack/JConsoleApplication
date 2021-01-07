package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.common.Commons;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.workspace.AdvancedDemo;
import xyz.scootaloo.console.app.workspace.LoginDemo;
import xyz.scootaloo.console.app.workspace.PluginDemo;
import xyz.scootaloo.console.app.workspace.QuicklyStart;

/**
 * 基础的控制台开发框架
 * 简化开发过程，自动装配命令，解析命令参数
 * 支持可选参数和必选参数，自动解析表单类
 *
 * 快速学习如何使用请移步到:
 * {@link xyz.scootaloo.console.app.workspace.QuicklyStart}
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Start {

    /**
     * 使用过程：
     * 1. 使用Commons.config()进行配置
     * 2. 在workspace目录下进行开发。
     * 3. 回到此类运行main方法，系统启动。
     *
     * @param args ignore
     */
    public static void main(String[] args) {
        ApplicationRunner.consoleApplication(
                Commons.config()
                        .appName("测试应用示例")
                        .printWelcome(true)
                        .prompt("example> ")
                        .printStackTrace(false)
                        .exitCmd(new String[] {"exit", "e.", "q"})
                        .maxHistory(128)
                        .editAuthorInfo()
                            .authorName("fd")
                            .email("~~")
                            .comment("备注")
                            .createDate("2020/12.27")
                            .updateDate("2021/1/6")
                            .ok()
                        .addInitCommands()
                            .getFromFile("conf.txt")
                            .add("hp")
                            .ok()
                        .addCommandFactories()
                            .add(QuicklyStart.class, true)
                            .add(AdvancedDemo.class, false)
                            .add(PluginDemo.class, false)
                            .add(LoginDemo.class, false)
                            .ok()
                        .build());
    }

}
