package xyz.scootaloo.console.app.support.utils;

import xyz.scootaloo.console.app.support.common.Colorful;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 执行反射操作时的一些便捷方法
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
        return father.isAssignableFrom(son.getClass());
    }

    /**
     * 将一个源对象的类属性值 拷贝到 目标对象的属性上
     * 只有属性名和类型一致才执行拷贝操作，否则跳过
     * @param source 原对象，提供属性
     * @param target 目标对象
     */
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

    /**
     * 找到此类中的无参构造方法，并使用此构造方法实例化出对象，
     * 假如这个类没有提供无参构造方法，则抛出异常。
     * @param clazz -
     * @return 此clazz类型的实例
     * @throws IllegalAccessException -
     * @throws InvocationTargetException -
     * @throws InstantiationException -
     */
    public static Object newInstance(Class<?> clazz) throws IllegalAccessException,
                                                            InvocationTargetException,
                                                            InstantiationException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            constructor.setAccessible(true);
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            }
        }
        throw new RuntimeException("类 `" + clazz.getSimpleName() + "` 没有提供无参构造方法，无法实例化");
    }

    /**
     * 判断两个类的属性是否一致
     * @param f1 -
     * @param f2 -
     * @return -
     */
    public static boolean sameType(Field f1, Field f2) {
        return f1.getGenericType().getTypeName().equals(f2.getGenericType().getTypeName());
    }

    public static void main(String[] args) {
        System.out.println(isExtendForm(int.class, List.class));
    }

}
