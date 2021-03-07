package xyz.scootaloo.console.app.anno;

import xyz.scootaloo.console.app.common.Console;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记命令方法 (可以用命令行调用的方法)
 *
 * @see Console#factories() 框架常用的注册工厂入口
 * @see xyz.scootaloo.console.app.Main 使用方式，以启动一个控制台应用示例
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {

    /**
     * @return 命令的类型
     */
    CmdType type() default CmdType.Cmd;

    /**
     * @return 命令的别名
     */
    String name() default "";

    /**
     * @see xyz.scootaloo.console.app.parser.NameableParameterParser
     * @return 参数解析方式 默认参数解析功能由系统实现
     */
    String parser() default "*";

    /**
     * @return 命令对应的类对象(type = CmdType.Parser时用)
     */
    Class<?>[] targets() default {};

    /**
     * @return 命令条件返回false时被输出(type = CmdType.Filter时用)
     */
    String onError() default "";

    /**
     * @return 优先级别，使用过滤器时可以修改这个属性
     */
    int order() default 5;

    /**
     * @return 命令的标签，用于查找 命令 find -t usr
     */
    String tag() default "usr";

}
