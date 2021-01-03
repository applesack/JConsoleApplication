package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.config.ConfigProvider;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

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
public class Start extends ConfigProvider {

    /**
     * 使用过程：
     * 1. 在此类的 {@link #register(DefaultValueConfigBuilder)} 方法中设置需要的配置。
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
                .appName("控制台应用示例")    // 应用的名称
                .appType(AppType.Standard) // 应用的类型
                .exitCmd(new String[] {"x", "q", "exit", "e."}) // 当控制台接收到此输入时退出
                .prompt("demo> ") // 控制台的提示符
                .basePack("workspace")  // 扫描当前类同包下的 workspace 包中的类
                .printWelcome(true)     // 是否在应用启动时输出欢迎界面
                .printStackTrace(false) // 是否在应用抛出异常时隐藏调用栈
                .editAuthorInfo() // 选择编辑作者信息
                    .authorName("fd") // 作者名称
                    .email("flutterdash@qq.com")  // 作者邮箱
                    .createDate("2020/12/28")     // 创建此应用的日期
                    .updateDate("2021/1/1")       // 修改此应用的日期
                    .comment("-------")           // 备注
                    .build()
                .build();
    }
}
