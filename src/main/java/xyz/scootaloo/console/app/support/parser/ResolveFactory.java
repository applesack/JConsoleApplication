package xyz.scootaloo.console.app.support.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 21:47
 */
public class ResolveFactory {

    private static final Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<>(16);

    static {
        putDefVal(int.class, Integer.class, 0);
        putDefVal(short.class, Short.class, 0);
        putDefVal(float.class, Float.class, 0);
        putDefVal(double.class, Double.class, 0);
        putDefVal(byte.class, Byte.class, 0);
        putDefVal(boolean.class, Boolean.class, false);
    }

    public static Object getDefVal(Class<?> type) {
        return DEFAULT_VALUE_MAP.getOrDefault(type, null);
    }

    private static void putDefVal(Class<?> type, Object value) {
        DEFAULT_VALUE_MAP.put(type, value);
    }

    private static void putDefVal(Class<?> type1, Class<?> type2, Object value) {
        DEFAULT_VALUE_MAP.put(type1, value);
        DEFAULT_VALUE_MAP.put(type2, value);
    }

}
