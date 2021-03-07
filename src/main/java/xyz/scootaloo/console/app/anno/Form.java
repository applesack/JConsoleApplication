package xyz.scootaloo.console.app.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类是表单类
 *
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:15
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Form {

    // 默认退出表单输入使用的命令 .
    String dftExtCmd() default ".";

}
