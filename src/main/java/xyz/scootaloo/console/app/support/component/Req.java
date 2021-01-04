package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 必选参数，作用于方法参数
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Req {

    // 参数的名称
    String value();

}
