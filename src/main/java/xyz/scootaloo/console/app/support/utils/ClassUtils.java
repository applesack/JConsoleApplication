package xyz.scootaloo.console.app.support.utils;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.common.Console;
import xyz.scootaloo.console.app.support.common.ResourceManager;
import xyz.scootaloo.console.app.support.parser.TransformFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 执行反射操作时的一些便捷方法
 * @author flutterdash@qq.com
 * @since 2020/12/27 17:08
 */
public abstract class ClassUtils {
    private static final Colorful cPrint = ResourceManager.getColorfulPrinter();
    private static final String DELIMITER = ",";

    public static void set(Object instance, String fieldName, Object value) {
        Class<?> clazz = instance.getClass();
        Field field = Console.ex(clazz::getDeclaredField, fieldName);
        if (field == null)
            return;
        field.setAccessible(true);
        Console.wDbEx(field::set, instance, value);
    }

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
                Field targetField = Console.ex(targetClazz::getDeclaredField, fieldName);
                if (targetField == null)
                    continue;
                targetField.setAccessible(true);
                if (!sameType(field, targetField))
                    throw new IllegalArgumentException("属性不一致");
                targetField.set(target, Console.ex(field::get, source));
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
     * 从Map中获取属性并拷贝到对象
     * @param instance 对象的实例
     * @param map 属性集
     * @param prop 目标实例是一个类属性，则这个参数为该类属性名，否则为null
     * @param functionMap 在转换失败时提供一个转换器
     */
    public static void loadPropFromMap(Object instance, Map<String, Object> map, String prop,
                                       Map<String, Function<Object, Object>> functionMap) {
        Class<?> iClazz;
        if (prop != null) {
            iClazz = instance.getClass();
            Field field = Console.ex(iClazz::getDeclaredField, prop);
            if (field == null)
                return;
            field.setAccessible(true);
            instance = Console.ex(field::get, instance);
            if (instance == null)
                return;
        }
        iClazz = instance.getClass();
        for (Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Field field = Console.ex(iClazz::getDeclaredField, key);
            if (field == null)
                continue;
            field.setAccessible(true);
            if (field.getType() == value.getClass()) {
                Console.wDbEx(field::set, instance, value);
            } else {
                if (functionMap.containsKey(key)) {
                    try {
                        field.set(instance, functionMap.get(key).apply(value));
                    } catch (Exception ignore) {
                        // 不符合转换条件时，将抛出异常，予以忽略
                    }
                }
            }
        }
    }

    // 判断两个类的属性是否一致
    public static boolean sameType(Field f1, Field f2) {
        return f1.getGenericType().getTypeName().equals(f2.getGenericType().getTypeName());
    }

    public static String getMethodInfo(Method method) {
        return method.getName() + '(' +
                Stream.of(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(",")) +
                ')' + ':' +
                method.getReturnType().getSimpleName();
    }

    // 获取方法泛型参数的实际类型 List<Integer> => Integer
    public static Class<?> getRawType(Type type) throws ClassNotFoundException {
        return Class.forName(((ParameterizedTypeImpl) type).getActualTypeArguments()[0].getTypeName(),
                false, ResourceManager.getLoader());
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

}
