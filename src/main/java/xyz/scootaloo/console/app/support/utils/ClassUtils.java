package xyz.scootaloo.console.app.support.utils;

import org.testng.annotations.Test;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.ResourceManager;
import xyz.scootaloo.console.app.support.parser.TransformFactory;

import java.lang.reflect.*;
import java.util.*;

/**
 * 执行反射操作时的一些便捷方法
 * @author flutterdash@qq.com
 * @since 2020/12/27 17:08
 */
public class ClassUtils {   
    private static final Colorful cPrint = ResourceManager.cPrint;
    private static final String DELIMITER = ",";

    /**
     * 判断一个类是否是另一个类的子类
     * @param son 子类
     * @param father 父类
     * @return 结果
     */
    public static boolean isExtendForm(Object son, Class<?> father) {
        if (son instanceof Class)
            return father.isAssignableFrom((Class<?>) son);
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

    // 判断两个类的属性是否一致
    public static boolean sameType(Field f1, Field f2) {
        return f1.getGenericType().getTypeName().equals(f2.getGenericType().getTypeName());
    }

    // 获取方法泛型参数的实际类型 List<Integer> => Integer
    public static Class<?> getRawType(Type type) throws ClassNotFoundException {
        return Class.forName(((ParameterizedTypeImpl) type).getActualTypeArguments()[0].getTypeName(),
                false, ResourceManager.loader);
    }

    //---------------------------------字符串向集合的转换----------------------------------------------

    // 生成泛型数组
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> T[] genArray(Class<T> type, String theArr) {
        String[] items = theArr.split(DELIMITER);
        T[] rArr = (T[]) Array.newInstance(type, items.length);
        for (int i = 0; i<rArr.length; i++) {
            rArr[i] = (T) TransformFactory.simpleTrans(items[i], type);
        }
        return rArr;
    }

    // 生成泛型列表
    public static <T> List<T> genList(Class<T> type, String rList) {
        List<T> list = new ArrayList<>();
        transEach(list, type, rList);
        return list;
    }

    public static <T> Set<T> genSet(Class<T> type, String rSet) {
        Set<T> set = new LinkedHashSet<>();
        transEach(set, type, rSet);
        return set;
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    private static <T> void transEach(Collection<T> collection, Class<T> type, String raw) {
        String[] items = raw.split(DELIMITER);
        for (String item : items) {
            collection.add((T) TransformFactory.simpleTrans(item, type));
        }
    }

    public static void main(String[] args) {
        String raw = "1,23,34,65,1";
        Integer[] arr = genArray(Integer.class, raw);
        Set<Integer> set = genSet(Integer.class, raw);
        System.out.println(isExtendForm(set, Set.class));
        System.out.println(Arrays.toString(arr));
        System.out.println(set);
    }

    @Test
    public void test() throws NoSuchMethodException, ClassNotFoundException {
        Method m = getMethod();
        final Type[] parameterTypes = m.getGenericParameterTypes();
        Type type = ((ParameterizedTypeImpl) parameterTypes[2]).getActualTypeArguments()[0];
        Class<?> tyClass = Class.forName(type.getTypeName(), false, this.getClass().getClassLoader());
        System.out.println();
    }

    public Method getMethod() throws NoSuchMethodException {
        Class<?> clazz = this.getClass();
        return clazz.getDeclaredMethod("fun", int.class, int[].class, List.class);
    }

    public List<String> fun(int a, int[] strings, List<Integer> nums) {
        return null;
    }

}
