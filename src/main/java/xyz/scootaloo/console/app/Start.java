package xyz.scootaloo.console.app;

import org.testng.annotations.Test;
import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.common.Commons;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.parser.Interpreter;
import xyz.scootaloo.console.app.support.parser.InvokeInfo;
import xyz.scootaloo.console.app.workspace.AdvancedDemo;
import xyz.scootaloo.console.app.workspace.LoginDemo;
import xyz.scootaloo.console.app.workspace.ListenerDemo;
import xyz.scootaloo.console.app.workspace.QuicklyStart;
import xyz.scootaloo.console.app.workspace.QuicklyStart.Student;

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
     * 启动一个控制台应用
     * 1. 使用Commons.config()进行配置
     * 2. 在workspace目录下进行开发。
     * 3. 回到此类运行此方法，系统启动。
     */
    @Test
    public void testConsoleApplication() {
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
                        // 增加命令工厂，enable参数决定是否启用该命令工厂，将false修改为true可以开启对应命令工厂的测试，
                        // 但是为了方便功能演示，建议测试以下几个类的时候，每次只有一个工厂类enable为true
                        .addCommandFactories()
                            .add(QuicklyStart.class, true)
                            .add(AdvancedDemo.class, false)
                            .add(ListenerDemo.class, false)
                            .add(LoginDemo.class, false)
                            .ok()
                        // 设置完成，应用启动
                        .build());
    }

    /**
     * 仅获取一个解释器，而不是从控制台上获取键盘输入
     * 动态地执行某类的方法，可以直接得到结果(返回的是包装类，包装类含有方法执行的信息)
     */
    @Test
    public void testInterpreter() {
        // 使用 Commons.simpleConf() 获取更精简的配置类
        Interpreter interpreter = ApplicationRunner.getInterpreter(Commons.simpleConf()
                .printStackTrace(false)
                .addFactory(QuicklyStart.class, true)
                .addFactory(AdvancedDemo.class, false)
                .addFactory(ListenerDemo.class, false)
                .addFactory(LoginDemo.class, false)
                .build());

        // 直接运行命令，得到结果的包装类
        InvokeInfo result1 = interpreter.interpret("add 11 12");
        System.out.println("执行 'add 11 12' 的结果: " + result1.get());

        // 使用参数运行，这里的args等于方法参数，也就是说这里可以看成是调用 add(11, 12)
        InvokeInfo result2 = interpreter.invoke("add", 11, 12);
        System.out.println("使用参数执行，结果: " + result2.get());

        // 解释器调用参数含有对象的方法时，字符串的占位符会触发等待键盘输入，如
//        InvokeInfo result3 = interpreter.interpret("stuAdd #"); // 在 main 方法中调用可以观察到

        // result3的方式调用参数中含有对象的方法，可能会引起线程阻塞，可以使用 invoke 方法传入对象调用
        // 或者实现自定义的类型转换器，参考 AdvancedDemo.resolveByte(Str) 方法
        InvokeInfo result4 = interpreter.invoke("stuAdd", new Student());
    }

}
