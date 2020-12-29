package xyz.scootaloo.console.app.support.utils;

import xyz.scootaloo.console.app.support.common.Colorful;

import java.lang.reflect.Field;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 17:08
 */
public class ClassUtils {   
    private static final Colorful cPrint = Colorful.instance;
    
    /**
     * 判断一个类是否是另一个类的子类
     * @param son 子类
     * @param father 父类
     * @return 结果
     */
    public static boolean isExtendForm(Object son, Class<?> father) {
        return father.isAssignableFrom(son.getClass()) || son.getClass().isInstance(father);
    }

    public static void copyProperties(Object source, Object target) {
        if (source == null || target == null)
            throw new IllegalArgumentException("either null." + "source:" + source + ", target:" + target);
        Class<?> sourceClazz = source.getClass();
        Class<?> targetClazz = target.getClass();

        for (Field field : sourceClazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                String fieldName = field.getName();
                Field targetField = targetClazz.getDeclaredField(fieldName);
                targetField.setAccessible(true);
                if (!sameType(field, targetField))
                    throw new IllegalArgumentException("属性不一致");
                targetField.set(target, field.get(source));
            } catch (NoSuchFieldException e) {
                cPrint.println(cPrint.red("拷贝属性时发生异常, 目标实例不具有此属性名: " + field.getName()));
            } catch (Exception e) {
                cPrint.println("拷贝属性时发生异常，已跳过，属性名:" + field.getName() + ". msg:" + e.getMessage());
            }
        }
    }

    public static boolean sameType(Field f1, Field f2) {
        return f1.getGenericType().getTypeName().equals(f2.getGenericType().getTypeName());
    }

}
