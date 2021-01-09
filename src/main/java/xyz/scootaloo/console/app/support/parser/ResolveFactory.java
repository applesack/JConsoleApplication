package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.component.Req;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析工厂
 * 将字符串的命令转换成Object数组
 * 将字符串格式的参数转换成方法的参数
 * 这里支持由方法参数注解中标记的可选参数和必选参数。
 *
 * 例如:
 * add 12 13
 * ->
 * add(int, int)
 * ;
 *
 * his -n=5 -s=hp
 * ->
 * history(int, String)
 * ;
 * @author flutterdash@qq.com
 * @since 2020/12/29 11:21
 */
public class ResolveFactory {

    /**
     * 解析逻辑实现的入口
     * @param method -
     * @param cmdline -
     * @return 包装的结果
     */
    public static ResultWrapper transform(Method method, List<String> cmdline) {
        if (method.getParameterCount() == 0)
            return ResultWrapper.success(null);
        Class<?>[] argTypes = method.getParameterTypes();
        Type[] genericTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnoArrays = method.getParameterAnnotations();
        List<Object> args = new ArrayList<>();

        List<WildcardArgument> wildcardArguments = new ArrayList<>();
        Map<String, Object> optMap = new HashMap<>();
        Map<String, Object> reqMap = new HashMap<>();
        cmdline = loadArgumentFromCmdline(cmdline, optMap, reqMap);

        Annotation anno;
        for (int i = 0; i<argTypes.length; i++) {
            Annotation[] curAnnoArr = parameterAnnoArrays[i];
            Class<?> curArgType = argTypes[i];
            if (curAnnoArr.length == 0) {
                if (cmdline.isEmpty())
                    return ResultWrapper.fail(new RuntimeException("命令不完整"));
                args.add(resolveArgument(cmdline.remove(0), argTypes[i], genericTypes[i]));
                continue;
            }

            anno = findAnnoFromArray(curAnnoArr, Opt.class);
            if (anno != null) {
                Opt option = (Opt) anno;
                if (getAndRemove(args, option.value(), curArgType, genericTypes[i], optMap)) {
                    if (option.value() == '*') {
                        wildcardArguments.add(new WildcardArgument(i, curArgType, genericTypes[i]));
                    }
                    if (!option.defVal().equals("")) {
                        args.set(args.size() - 1, TransformFactory
                                .simpleTrans(option.defVal(), curArgType));
                    }
                }
            }
            anno = findAnnoFromArray(curAnnoArr, Req.class);
            if (anno != null) {
                Req required = (Req) anno;
                if (getAndRemove(args, required.value(), curArgType, genericTypes[i], reqMap)) {
                    return ResultWrapper.fail(new RuntimeException("缺少必选参数[" + required.value() + "]"));
                }
            }
        }

        if (!wildcardArguments.isEmpty()) {
            for (WildcardArgument wildcardArgument : wildcardArguments) {
                if (!cmdline.isEmpty()) {
                    args.set(wildcardArgument.idx,
                            resolveArgument(cmdline.remove(0), wildcardArgument.type,
                                    wildcardArgument.generic));
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
                    if (node.val.equals("*"))
                        optMap.put(node.key, null);
                    else
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

    private static Object resolveArgument(Object value, Class<?> classType, Type genericType) {
        try {
            return TransformFactory.resolveArgument(value, classType, genericType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("格式解析时异常: " + e.getMessage());
        }
    }

    private static boolean getAndRemove(List<Object> arg, Object key,
                                        Class<?> classType, Type genericType, Map<?, Object> map) {
        String rKey = String.valueOf(key);
        if (map.containsKey(String.valueOf(rKey))) {
            arg.add(resolveArgument(map.get(rKey), classType, genericType));
            map.remove(key);
            return false;
        } else {
            arg.add(TransformFactory.getDefVal(classType));
            return true;
        }
    }

    // ------------------------------------POJO--------------------------------------------

    public static class ResultWrapper {

        public final boolean success;
        public final Object[] args;
        public final Exception ex;

        public static ResultWrapper success(List<Object> argList) {
            return new ResultWrapper(true, argList, null);
        }

        public static ResultWrapper fail(Exception e) {
            return new ResultWrapper(false, null, e);
        }

        private ResultWrapper(boolean success, List<Object> argList, Exception ex) {
            this.ex = ex;
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

    private static class WildcardArgument {

        final int idx;
        final Class<?> type;
        final Type generic;

        public WildcardArgument(int idx, Class<?> type, Type generic) {
            this.idx = idx;
            this.type = type;
            this.generic = generic;
        }

    }

}
