package xyz.scootaloo.console.app.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个命令行的参数
 *
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Opt {

    /**
     * @return 命令行参数的简称
     */
    char value();

    /**
     * @return 命令行参数的全称
     */
    String fullName() default "";

    /**
     * @return 此命令行参数是否是必选的
     */
    boolean required() default false;

    /**
     * @return 假如这里返回 true , 则对应的方法参数将拼合余下的命令参数
     */
    boolean joint() default false;

    /**
     * @return 当可选参数未选中时可以提供一个默认值
     */
    String dftVal() default "";

}
