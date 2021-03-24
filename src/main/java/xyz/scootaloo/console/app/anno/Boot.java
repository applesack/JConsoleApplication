package xyz.scootaloo.console.app.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类是启动类
 *
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Boot {

    String name() default "";

}
