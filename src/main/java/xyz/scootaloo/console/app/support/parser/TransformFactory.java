package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.component.Req;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static ResultWrapper transform(Method method, List<String> cmdline) {
        if (method.getParameterCount() == 0)
            return ResultWrapper.success(null);
        Class<?>[] argTypes = method.getParameterTypes();
        Annotation[][] parameterAnnoArrays = method.getParameterAnnotations();
        List<Object> args = new ArrayList<>();

        Map<String, Object> optMap = new HashMap<>();
        Map<String, Object> reqMap = new HashMap<>();
        cmdline = loadArgumentFromCmdline(cmdline, optMap, reqMap);

        Annotation anno;
        for (int i = 0; i<argTypes.length; i++) {
            Annotation[] curAnnoArr = parameterAnnoArrays[i];
            Class<?> curArgType = argTypes[i];
            if (curAnnoArr.length == 0) {
                if (cmdline.isEmpty())
                    return ResultWrapper.fail("命令不完整");
                args.add(resolveArgument(cmdline.remove(0), curArgType));
                continue;
            }

            anno = findAnnoFromArray(curAnnoArr, Opt.class);
            if (anno != null) {
                Opt option = (Opt) anno;
                getAndRemove(args, option.value(), curArgType, optMap);
            }
            anno = findAnnoFromArray(curAnnoArr, Req.class);
            if (anno != null) {
                Req required = (Req) anno;
                if (!getAndRemove(args, required.value(), curArgType, reqMap)) {
                    return ResultWrapper.fail("缺少必选参数[" + required.value() + "]");
                }
            }
        }

        return ResultWrapper.success(args);
    }

    private static List<String> loadArgumentFromCmdline(List<String> cmdline,
                                                Map<String, Object> optMap,
                                                Map<String, Object> reqMap) {
        List<String> pureValues = new ArrayList<>();
        for (String segment : cmdline) {
            boolean isAnnoArg = false;
            if (segment == null || segment.equals(""))
                continue;
            if (segment.startsWith("--")) {
                isAnnoArg = true;
                Node node = getValueForKVPair(segment, 2);
                if (node != null)
                    reqMap.put(node.key, node.val);
            } else if (segment.startsWith("-")) {
                isAnnoArg = true;
                Node node = getValueForKVPair(segment, 1);
                if (node != null) {
                    optMap.put(node.key, node.val);
                } else {
                    segment = segment.substring(1);
                    if (segment.length() > 0) {
                        for (char opt : segment.toCharArray()) {
                            optMap.put(String.valueOf(opt), true);
                        }
                    }
                }
            }

            if (!isAnnoArg)
                pureValues.add(segment);
        }
        return pureValues;
    }

    private static <T> Annotation findAnnoFromArray(Annotation[] annotations, Class<T> targetAnno) {
        for (Annotation anno : annotations) {
            if (anno.annotationType() == targetAnno) {
                return anno;
            }
        }
        return null;
    }

    private static Node getValueForKVPair(String segment, int prefixIdx) {
        segment = segment.substring(prefixIdx);
        int delimiter = segment.indexOf('=');
        if (delimiter == -1)
            return null;
        String[] segments = segment.split("=");
        if (segments.length != 2)
            return null;
        return new Node(segments[0], segments[1]);
    }

    private static Object resolveArgument(Object value, Class<?> type) {
        if (type.isArray() || ClassUtils.isExtendForm(type, List.class)) {
            return resolveArray(value, type);
        } else {
            return simpleTrans(String.valueOf(value), type);
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

    private static Object resolveArray(Object value, Class<?> type) {
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

    private static boolean getAndRemove(List<Object> arg, Object key,
                                        Class<?> type, Map<?, Object> map) {
        String rKey = String.valueOf(key);
        if (map.containsKey(String.valueOf(rKey))) {
            arg.add(resolveArgument(map.get(rKey), type));
            map.remove(key);
            return true;
        } else {
            arg.add(ResolveFactory.getDefVal(type));
            return false;
        }
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

    private static class Node {

        final String key;
        final String val;

        public Node(String key, String val) {
            this.key = key;
            this.val = val;
        }

    }

}
