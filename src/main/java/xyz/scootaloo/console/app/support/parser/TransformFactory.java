package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.component.Opt;
import xyz.scootaloo.console.app.support.component.Req;

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

    public static ResultWrapper transform(Method method, List<String> cmdline) {
        if (method.getParameterCount() == 0)
            return ResultWrapper.success(null);
        Class<?>[] argTypes = method.getParameterTypes();
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
                    return ResultWrapper.fail("命令不完整");
                args.add(resolveArgument(cmdline.remove(0), curArgType));
                continue;
            }

            anno = findAnnoFromArray(curAnnoArr, Opt.class);
            if (anno != null) {
                Opt option = (Opt) anno;
                if (getAndRemove(args, option.value(), curArgType, optMap)) {
                    if (option.value() == '*') {
                        wildcardArguments.add(new WildcardArgument(i, curArgType));
                    }
                    if (!option.defVal().equals("")) {
                        args.set(args.size() - 1, ResolveFactory
                                .simpleTrans(option.defVal(), curArgType));
                    }
                }
            }
            anno = findAnnoFromArray(curAnnoArr, Req.class);
            if (anno != null) {
                Req required = (Req) anno;
                if (getAndRemove(args, required.value(), curArgType, reqMap)) {
                    return ResultWrapper.fail("缺少必选参数[" + required.value() + "]");
                }
            }
        }

        if (!wildcardArguments.isEmpty()) {
            for (WildcardArgument wildcardArgument : wildcardArguments) {
                if (!cmdline.isEmpty()) {
                    args.set(wildcardArgument.idx,
                            resolveArgument(cmdline.remove(0), wildcardArgument.type));
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

    private static Object resolveArgument(Object value, Class<?> type) {
        return ResolveFactory.resolveArgument(value, type);
    }

    private static boolean getAndRemove(List<Object> arg, Object key,
                                        Class<?> type, Map<?, Object> map) {
        String rKey = String.valueOf(key);
        if (map.containsKey(String.valueOf(rKey))) {
            arg.add(resolveArgument(map.get(rKey), type));
            map.remove(key);
            return false;
        } else {
            arg.add(ResolveFactory.getDefVal(type));
            return true;
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

    private static class WildcardArgument {

        final int idx;
        final Class<?> type;

        public WildcardArgument(int idx, Class<?> type) {
            this.idx = idx;
            this.type = type;
        }

    }

}
