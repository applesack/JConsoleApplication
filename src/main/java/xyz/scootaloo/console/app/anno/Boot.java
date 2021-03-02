package xyz.scootaloo.console.app.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅用于标记一个类是启动类<br>
 * 这个注解只存在于源码中，不会被载入到虚拟机中。
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:59
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Boot {

}
