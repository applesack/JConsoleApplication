package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可选参数，标记于方法参数
 * @author flutterdash@qq.com
 * @since 2020/12/29 19:12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Opt {

    // 此可选参数的标记
    char value();

    // 当可选参数未选中时可以提供一个默认值
    String defVal() default "";

}
