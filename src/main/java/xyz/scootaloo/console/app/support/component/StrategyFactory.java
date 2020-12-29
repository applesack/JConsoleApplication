package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StrategyFactory {

    boolean enable() default true;

}
