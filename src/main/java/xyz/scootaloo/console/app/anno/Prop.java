package xyz.scootaloo.console.app.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作用于表单类的类属性上
 * @see Form 使用此注解时，类必须标记为 @Form
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Prop {

    /**
     * @return 输入每个属性值时候的提示
     */
    String prompt() default "";

    /**
     * @return 是否必须，假如为false则回车会跳过此属性的输入，否则只有在输入了有效数值才能继续
     */
    boolean isRequired() default false;

}
