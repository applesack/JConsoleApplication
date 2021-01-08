package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令方法标记
 * 将这个注解标记在一个实例方法上，就可以被系统扫描到，
 * 前提是这个实例方法所在的类被添加到了配置中
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {
    // 命令的类型
    CmdType type() default CmdType.Cmd;

    // 命令的别名
    String name() default "";
    // 命令对应的类对象(type = CmdType.Parser时用)
    Class<?>[] targets() default {};
    // 前置方法返回false时被输出(type = CmdType.Pre时用)
    String onError() default "";

    // 命令的优先级别，目前这个好像没什么用
    int order() default 5;

}
