package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    public static Object resolveArgument(Object value, Class<?> type) {
        if (type.isArray() || ClassUtils.isExtendForm(type, List.class)) {
            return resolveArray(value, type);
        } else {
            return simpleTrans(String.valueOf(value), type);
        }
    }

    // 简易的转换，转换基本类型
    public static Object simpleTrans(Object value, Class<?> type) {
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

    // todo 解析数组
    private static Object resolveArray(Object value, Class<?> type) {
        return value == type;
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
