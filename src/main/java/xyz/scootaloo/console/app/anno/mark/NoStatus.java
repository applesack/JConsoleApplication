package xyz.scootaloo.console.app.anno.mark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 无状态的资源，不会因为多个连接改变状态
 * @author flutterdash@qq.com
 * @since 2021/3/2 17:41
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface NoStatus {

    String value() default "";

}
