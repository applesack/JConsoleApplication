package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

/**
 * 转换工厂
 * 实现字符串到指定类型的转换
 * @author flutterdash@qq.com
 * @since 2020/12/29 21:47
 */
public class TransformFactory {

    private static final Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<>(16);
    private static final Map<Class<?>, Function<String , Object>> STR_RESOLVE_MAP = new HashMap<>(16);

    static {
        putDefVal(str -> str);
        putDefVal(int.class    , Integer.class, 0, Integer::parseInt);
        putDefVal(short.class  , Short.class  , 0, Short::parseShort);
        putDefVal(float.class  , Float.class  , 0, Float::parseFloat);
        putDefVal(double.class , Double.class , 0, Double::parseDouble);
        putDefVal(byte.class   , Byte.class   , 0, Byte::parseByte);
        putDefVal(boolean.class, Boolean.class, false,
                str -> {
                    if (str == null || str.equals(""))
                        return false;
                    str = str.toLowerCase(Locale.ROOT);
                    return str.startsWith("t");
                }
        );
    }

    // 获取此类型的默认值
    public static Object getDefVal(Class<?> type) {
        return DEFAULT_VALUE_MAP.getOrDefault(type, null);
    }

    // 将value转换为type的类型
    public static Object resolveArgument(Object value, Class<?> classType, Type genericType) throws Exception {
        if (classType.isArray() || ClassUtils.isExtendForm(classType, Collection.class)) {
            return resolveArray(value, classType, genericType);
        } else {
            return simpleTrans(String.valueOf(value), classType);
        }
    }

    // 简易的转换，转换基本类型
    public static Object simpleTrans(Object value, Class<?> type) {
        // 尝试进行占位符替换
        Object pObj = resolvePlaceholder(value, type);
        if (pObj != null)
            return pObj;

        Function<String, Object> convertor = STR_RESOLVE_MAP.get(type);
        if (convertor != null) {
            return convertor.apply(String.valueOf(value));
        } else {
            // 处理表单
            try {
                FormHelper.ObjWrapper wrapper = FormHelper.checkAndGet(type);
                if (wrapper.success)
                    return wrapper.instance;
                return DEFAULT_VALUE_MAP.getOrDefault(type, null);
            } catch (Exception e) {
                e.printStackTrace();
                return DEFAULT_VALUE_MAP.getOrDefault(type, null);
            }
        }
    }

    // 添加某类型的解析器，@Cmd的type=Parser的方法会被调用到这里来
    public static void addParser(Function<String, Object> parser, Class<?> ... types) {
        if (parser == null || types == null || types.length == 0)
            return;
        for (Class<?> type : types) {
            if (type == null)
                continue;
            STR_RESOLVE_MAP.put(type, parser);
        }
    }

    // 解析数组和集合
    private static Object resolveArray(Object value, Class<?> type, Type genericType) throws ClassNotFoundException {
        if (type.isArray()) {
            return ClassUtils.genArray(type.getComponentType(), (String) value);
        } else {
            if (ClassUtils.isExtendForm(type, Set.class)) {
                return ClassUtils.genSet(ClassUtils.getRawType(genericType), (String) value);
            } else if (ClassUtils.isExtendForm(type, List.class)) {
                return ClassUtils.genList(ClassUtils.getRawType(genericType), (String) value);
            } else {
                throw new RuntimeException("暂不支持的数据结构: " + type.getName());
            }
        }
    }

    private static Object resolvePlaceholder(Object value, Class<?> type) {
        if (!value.equals(PropertyManager.placeholder))
            return null;
        Object placeholderObj = PropertyManager.get();
        if (placeholderObj != null && placeholderObj.getClass() == type) {
            return placeholderObj;
        }
        return null;
    }

    private static void putDefVal(Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(String.class, null);
        STR_RESOLVE_MAP.put(String.class, convertor);
    }

    private static void putDefVal(Class<?> type1, Class<?> type2, Object value,
                                  Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(type1, value);
        DEFAULT_VALUE_MAP.put(type2, value);

        STR_RESOLVE_MAP.put(type1, convertor);
        STR_RESOLVE_MAP.put(type2, convertor);
    }

}
