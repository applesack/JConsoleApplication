package xyz.scootaloo.console.app;

import org.testng.annotations.Test;
import xyz.scootaloo.console.app.support.application.ApplicationRunner;
import xyz.scootaloo.console.app.support.common.Console;
import xyz.scootaloo.console.app.support.component.Boot;
import xyz.scootaloo.console.app.support.parser.Interpreter;
import xyz.scootaloo.console.app.support.parser.InvokeInfo;
import xyz.scootaloo.console.app.support.parser.preset.SimpleParameterParser;
import xyz.scootaloo.console.app.workspace.*;
import xyz.scootaloo.console.app.workspace.QuickStart.Student;

/**
 * 控制台开发框架
 * 简化开发过程，自动装配命令，解析命令参数
 * 支持可选参数和必选参数，自动解析表单类
 *
 * 快速学习如何使用请移步到:
 * {@link QuickStart}
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Start {

    /**
     * 启动一个控制台应用
     * 1. 使用Commons.factories()进行配置
     * 2. 假如需要额外的配置，在resource目录下修改console.yml的配置内容
     * 3. 在workspace目录下进行开发。
     * 4. 回到此类运行此 main 方法，系统启动。
     */
    public static void main(String[] args) {
        ApplicationRunner.consoleApplication(
                Console.factories()
                    .addFactories()
                        .add(QuickStart::new, true)
                        .add(AdvancedDemo::new, true)
                        .add(ListenerDemo::new, false)
                        .add(LoginDemo::new, false)
                        .add(HelpForDemo::new, true)
                        .add(SimpleParameterParser.INSTANCE, true)
                    .ok()
                .build()
        ).run();
    }

    /**
     * 仅获取一个解释器，而不是从控制台上获取键盘输入
     * 动态地执行某类的方法，可以直接得到结果(返回的是包装类，包装类含有方法执行的信息)
     */
    @Test
    public void testInterpreter() {
        // 使用 Commons.simpleConf() 获取更精简的配置类
        Interpreter interpreter = ApplicationRunner.getInterpreter(
                Console.factories()
                    .addFactories()
                        .add(QuickStart::new, true)
                        .ok()
                    .build());

        // 直接运行命令，得到结果的包装类
        InvokeInfo result1 = interpreter.interpret("add 11 12");
        System.out.println("执行 'add 11 12' 的结果: " + result1.get());

        // 使用参数运行，这里的args等于方法参数，也就是说这里可以看成是调用 add(11, 12)
        InvokeInfo result2 = interpreter.invoke("add", 11, 12);
        System.out.println("使用参数执行，结果: " + result2.get());

        // 解释器调用参数含有对象的方法时，字符串命令中的占位符会触发等待键盘输入，如
//        InvokeInfo result3 = interpreter.interpret("stuAdd #"); // 在 main 方法中调用可以观察到

        // result3的方式调用参数中含有对象的方法，某些场景下可能会引起线程阻塞，可以使用 invoke 方法传入对象调用
        // 或者实现自定义的类型转换器，参考 AdvancedDemo.resolveByte(Str) 方法
        InvokeInfo result4 = interpreter.invoke("addStu", new Student());
        System.out.println("result4: " + result4.get());

        // 在解释器中使用变量占位符
        InvokeInfo result5 = interpreter.interpret("echo -v ${rand.int(10,15)}");
        System.out.println("\"echo -v ${rand.int(10,15)}\"的结果是: " + result5.get());

        System.out.println("\n----------------------------------------------------\n");

        // 变量功能在解释器中的使用
        // 将这个随机整型做为 "randNumber" 这个键的值
        boolean flag = interpreter.set("randNumber", "echo 使用echo时两边的内容${rand.int(10,15)}都被忽略了");
        // 检查设置情况
        if (flag)
            System.out.println("设置成功");
        // 现在可以获取到这个值了，使用get (不需要占位符) 或者 echo (需要占位符)
        InvokeInfo result6 = interpreter.interpret("get randNumber"); // else: echo ${randNumber}
        System.out.println("randNumber is " + result6.get());
    }

}
