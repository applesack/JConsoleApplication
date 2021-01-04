package xyz.scootaloo.console.app.support.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类是命令工厂，系统将从含有此标记是类中扫描所有方法
 * @see Cmd
 * --------------------------
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandFactory {

    // 此工厂是否启用，当系统内有多个命令工厂可以修改这个属性来决定每次启动时扫描指定的命令工厂
    boolean enable() default true;

}
