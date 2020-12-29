package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/29 11:21
 */
public class TransformFactory {

    private static final Class<?>[] RESOLVABLE_TYPES =  {
            int.class, Integer.class,
            boolean.class, Boolean.class,
            float.class, Float.class,
            double.class, Double.class,
            short.class, Short.class,
            byte.class, Boolean.class,
            String.class
    };

    public static ResultWrapper transform(Method method, List<String> listArgs) {
        if (method.getParameterCount() == 0)
            return ResultWrapper.success(null);
        Class<?>[] argTypes = method.getParameterTypes();
        List<Object> args = new ArrayList<>();

        if (listArgs.size() != argTypes.length)
            throw new IllegalArgumentException("参数数量不匹配");
        for (int i = 0; i<argTypes.length; i++) {
            args.add(resolveArgument(listArgs.get(i), argTypes[i]));
        }

        return ResultWrapper.success(args);
    }

    private static Object resolveArgument(String value, Class<?> type) {
        if (type.isArray() || ClassUtils.isExtendForm(type, List.class)) {
            return resolveArray(value, type);
        } else {
            return simpleTrans(value, type);
        }
    }

    private static Object simpleTrans(String value, Class<?> type) {
        if (matchAnyTypes(type, byte.class, Byte.class))
            return Byte.parseByte(value);
        if (matchAnyTypes(type, short.class, Short.class))
            return Short.parseShort(value);
        if (matchAnyTypes(type, int.class, Integer.class))
            return Integer.parseInt(value);
        if (matchAnyTypes(type, float.class, Float.class))
            return Float.parseFloat(value);
        if (matchAnyTypes(type, double.class, Double.class))
            return Double.parseDouble(value);
        if (matchAnyTypes(type, boolean.class, Boolean.class))
            return Boolean.parseBoolean(value);
        return value;
    }

    private static Object resolveArray(String value, Class<?> type) {
        return null;
    }

    private static boolean matchAnyType(Class<?> value) {
        return matchAnyTypes(value, RESOLVABLE_TYPES);
    }

    private static boolean matchAnyTypes(Class<?> value, Class<?> ... types) {
        for (Class<?> type : types) {
            if (value == type)
                return true;
        }
        return false;
    }

    public static void main(String[] args) {

    }

    public static class ResultWrapper {

        public final boolean success;
        public final Object[] args;
        public final String msg;

        public static ResultWrapper success(List<Object> argList) {
            return new ResultWrapper(true, argList, null);
        }

        public static ResultWrapper fail(String msg) {
            return new ResultWrapper(false, null, msg);
        }

        private ResultWrapper(boolean success, List<Object> argList, String msg) {
            this.msg = msg;
            this.success = success;
            if (argList != null)
                this.args = argList.toArray();
            else
                this.args = null;
        }

    }

}
