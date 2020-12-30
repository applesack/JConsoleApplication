package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 21:47
 */
public class ResolveFactory {

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

    public static Object getDefVal(Class<?> type) {
        return DEFAULT_VALUE_MAP.getOrDefault(type, null);
    }

    public static Object resolveArgument(Object value, Class<?> type) {
        if (type.isArray() || ClassUtils.isExtendForm(type, List.class)) {
            return resolveArray(value, type);
        } else {
            return simpleTrans(String.valueOf(value), type, true);
        }
    }

    public static Object simpleTrans(Object value, Class<?> type, boolean doContinue) {
        Function<String, Object> convertor = STR_RESOLVE_MAP.get(type);
        if (convertor != null) {
            return convertor.apply(String.valueOf(value));
        } else {
            // todo 可能是表单
            return DEFAULT_VALUE_MAP.getOrDefault(type, null);
        }
    }

    // todo
    private static Object resolveArray(Object value, Class<?> type) {
        return value == type;
    }


    private static void putDefVal(Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(String.class, null);
        STR_RESOLVE_MAP.put(String.class, convertor);
    }

    private static void putDefVal(Class<?> type1, Class<?> type2, Object value, Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(type1, value);
        DEFAULT_VALUE_MAP.put(type2, value);

        STR_RESOLVE_MAP.put(type1, convertor);
        STR_RESOLVE_MAP.put(type2, convertor);
    }

}
