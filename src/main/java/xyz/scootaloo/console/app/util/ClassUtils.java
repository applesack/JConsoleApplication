package xyz.scootaloo.console.app.util;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.Colorful;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.TransformFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 执行反射操作时的一些便捷方法
 * @author flutterdash@qq.com
 * @since 2020/12/27 17:08
 */
public final class ClassUtils {
    private static final Colorful color = ResourceManager.getColorful();
    private static final Console console = ResourceManager.getConsole();
    private static final String DELIMITER = ",";
    private static final Set<Class<?>> BOXING_SET = new LinkedHashSet<>();

    static {
        BOXING_SET.add(Integer.class);
        BOXING_SET.add(Double.class);
        BOXING_SET.add(Boolean.class);
        BOXING_SET.add(Byte.class);
        BOXING_SET.add(Float.class);
        BOXING_SET.add(Long.class);
    }

    public static void set(Object instance, String fieldName, Object value) {
        Class<?> clazz = instance.getClass();
        Field field = Console.ex(clazz::getDeclaredField, fieldName);
        if (field == null)
            return;
        field.setAccessible(true);
        Console.wDbEx(field::set, instance, value);
    }

    /**
     * 获取调用者的实例
     * @param self true 自身所在类的实例， false 调用此方法的调用者的实例
     * @return 实例
     */
    public static Object instance(boolean self) {
        StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
        int depth = self ? 2 : 3;
        String invoker = callStack[depth].getClassName();
        try {
            Class<?> BOOT_CLAZZ = Class.forName(invoker);
            return newInstance(BOOT_CLAZZ);
        } catch (ClassNotFoundException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            console.exit0("解析异常，无法实例化类: " + invoker);
            return null;
        }
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
                color.println("拷贝属性时发生异常，已跳过，属性名:" + field.getName() + ". msg:" + e.getMessage());
            }
        }
    }

    public static Supplier<Object> facSupplier(Class<?> factory) {
        return () -> Console.ex(ClassUtils::newInstance, factory);
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
     * @param properties 属性集
     * @param prop 目标实例是一个类属性，则这个参数为该类属性名，否则为null
     * @param functionMap 在转换失败时提供一个转换器
     */
    public static void loadPropFromMap(Object instance, Map<String, Object> properties, String prop,
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
        for (Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Field field = Console.ex(iClazz::getDeclaredField, key);
            if (field == null)
                continue;
            field.setAccessible(true);
            Class<?> propType = value.getClass();
            if (propType == field.getType() || BOXING_SET.contains(propType)) {
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

    // 返回方法的信息，方法名(参数):返回值
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

    // 生成泛型集
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
