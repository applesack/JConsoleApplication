package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 命令方法标记
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {

    CmdType type() default CmdType.Cmd;

    String name() default "";
    Class<?>[] targets() default {};
    String onError() default "";

    int order() default 5;

}
