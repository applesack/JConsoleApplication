package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.common.Commons;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.workspace.AdvancedDemo;
import xyz.scootaloo.console.app.workspace.LoginDemo;
import xyz.scootaloo.console.app.workspace.PluginDemo;
import xyz.scootaloo.console.app.workspace.QuicklyStart;

/**
 * 控制台开发框架
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
                        // 应用信息
                        .appName("测试应用示例") // 应用的名称
                        .printWelcome(true)   // 是否打印欢迎信息
                        .prompt("example> ")  // 控制台输入的提示符
                        .printStackTrace(false) // 遇到异常时是否打印调用栈
                        .exitCmd(new String[] {"exit", "e.", "q"}) // 使用这些命令可以退出应用
                        .maxHistory(128) // 最多保存的历史记录，
                        // 编辑作者信息，当printWelcome设置为false时，这些信息不会被输出
                        .editAuthorInfo()
                            .authorName("fd")
                            .email("~~")
                            .comment("备注")
                            .createDate("2020/12/27")
                            .updateDate("2021/1/6")
                            .ok()
                        // 设置系统启动时执行的命令
                        .addInitCommands()
                            .getFromFile("init.txt") // 从文件中读取
                            .add("help") // 系统启动时执行 help 命令
                            .ok()
                        // 增加命令工厂，在这里将
                        .addCommandFactories()
                            .add(QuicklyStart.class, true)
                            .add(AdvancedDemo.class, false)
                            .add(PluginDemo.class, false)
                            .add(LoginDemo.class, false)
                            .ok()
                        // 设置完成
                        .build());
    }

}
