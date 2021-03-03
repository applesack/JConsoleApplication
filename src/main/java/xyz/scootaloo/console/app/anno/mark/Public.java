package xyz.scootaloo.console.app.anno.mark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述一个资源是公有的，对于所有使用者可见
 * @author flutterdash@qq.com
 * @since 2021/3/2 17:29
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Public {

    String value() default "";

}
