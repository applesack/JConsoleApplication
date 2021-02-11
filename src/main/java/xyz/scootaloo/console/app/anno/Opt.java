package xyz.scootaloo.console.app.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法参数，配合参数解析器来实现特定的功能。
 * 以下的描述都只针对于默认的参数解析器
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Opt {

    // 此可选参数的标记
    char value();

    // 命令参数的全称
    String fullName() default "";

    // 是否是必须的
    boolean required() default false;

    // 假如这个参数为 true , 则对应的方法参数将拼合余下的命令参数
    boolean joint() default false;

    // 当可选参数未选中时可以提供一个默认值
    String dftVal() default "";

}
