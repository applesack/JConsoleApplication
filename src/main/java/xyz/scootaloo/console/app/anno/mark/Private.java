package xyz.scootaloo.console.app.anno.mark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述一个资源是私有的，每当用户创建连接，都会为该用户创建一份实例，或为该用户分配一些资源。
 * @author flutterdash@qq.com
 * @since 2021/3/2 17:32
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Private {

    String value() default "";

}
