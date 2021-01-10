package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.component.Opt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

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
    // 临时占位符
    private static final String PLACEHOLDER = "*";

    /**
     * 解析逻辑实现的入口
     * @param method 方法对象
     * @param cmdline 调用此方法使用的命令参数
     * @return 包装的结果
     */
    public static ResultWrapper transform(Method method, List<String> cmdline) {
        if (method.getParameterCount() == 0)
            return ResultWrapper.success(null);
        Class<?>[] argTypes = method.getParameterTypes();                      // 参数类型数组
        Type[] genericTypes = method.getGenericParameterTypes();               // 参数泛型类型数组
        Annotation[][] parameterAnnoArrays = method.getParameterAnnotations(); // 参数注解数组
        List<Object> args = new ArrayList<>();                                 // 最终可供Method对象invoke的参数
        Set<Character> shortParamsSet = doGetAllParameter(parameterAnnoArrays);  // 由注解中的简写命令参数名构成的集

        List<WildcardArgument> wildcardArguments = new ArrayList<>();
        // key=命令参数名 value=命令参数值
        Map<String, Object> optMap = new HashMap<>(); // 参数map
        cmdline = loadArgumentFromCmdline(cmdline, optMap, shortParamsSet);

        Annotation anno;
        for (int i = 0; i<argTypes.length; i++) {
            Annotation[] curAnnoArr = parameterAnnoArrays[i];
            Class<?> curArgType = argTypes[i];
            anno = findOptFromArray(curAnnoArr);
            // 当前这个参数没有注解，尝试将一个无名参数转换到这里
            if (anno == null) {
                if (cmdline.isEmpty())
                    return ResultWrapper.fail(new RuntimeException("命令不完整"));
                args.add(resolveArgument(cmdline.remove(0), argTypes[i], genericTypes[i]));
                continue;
            }

            // 只处理 @Opt 注解
            Opt option = (Opt) anno;
            Set<String> paramsSet = new LinkedHashSet<>();
            paramsSet.add(String.valueOf(option.value()));
            if (!option.fullName().isEmpty())
                paramsSet.add(option.fullName());

            // 没有此参数
            if (getAndRemove(args, paramsSet, curArgType, genericTypes[i], optMap)) {
                if (option.required())
                    return ResultWrapper.fail(new RuntimeException("缺少必要的参数: -" + option.value()));
                // 给这个位置的参数做一个标记，假如处理完还有多余的参数就填补到这个位置来
                wildcardArguments.add(new WildcardArgument(i, curArgType, genericTypes[i]));
                // 在 getAndRemove方法中已经处理了类型默认值的情况，这里处理用户给定的自定义默认值
                if (!option.defVal().equals("")) {
                    args.set(args.size() - 1, TransformFactory
                            .simpleTrans(option.defVal(), curArgType));
                }
            }
        }

        // 处理多余的命令参数
        if (!wildcardArguments.isEmpty()) {
            for (WildcardArgument wildcardArgument : wildcardArguments) {
                // 从剩余的命令参数列表中，依次填充到这些未选中的方法参数上
                if (!cmdline.isEmpty()) {
                    args.set(wildcardArgument.idx,
                            resolveArgument(cmdline.remove(0), wildcardArgument.type,
                                    wildcardArgument.generic));
                }
            }
        }

        return ResultWrapper.success(args);
    }

    private static Set<Character> doGetAllParameter(Annotation[][] parameterAnnoArrays) {
        Set<Character> parameterSet = new LinkedHashSet<>();
        for (Annotation[] pAnnoArray : parameterAnnoArrays) {
            Opt opt = (Opt) findOptFromArray(pAnnoArray);
            if (opt == null)
                continue;
            parameterSet.add(opt.value());
        }
        return parameterSet;
    }

    private static List<String> loadArgumentFromCmdline(List<String> cmdline,
                                                        Map<String, Object> optMap,
                                                        Set<Character> shortParamsSet) {
        LinkedList<String> retainList = new LinkedList<>();
        for (int i = 0; i<cmdline.size(); i++) {
            String curSegment = cmdline.get(i);
            if (!curSegment.startsWith("-")) {
                retainList.addLast(curSegment);
                continue;
            }

            int prefixCount = curSegment.startsWith("--") ? 2 : 1;
            curSegment = curSegment.substring(prefixCount);
            if (curSegment.isEmpty())
                continue;
            String nextSeg = PLACEHOLDER;
            if (isContainsAll(curSegment, shortParamsSet)) {
                for (char shortParam : curSegment.toCharArray()) {
                    optMap.put(String.valueOf(shortParam), true);
                }
                continue;
            }
            if (i < cmdline.size() - 1 && !cmdline.get(i + 1).startsWith("-")) {
                nextSeg = cmdline.get(i + 1);
                i++;
            }
            optMap.put(curSegment, nextSeg);
        }
        return retainList;
    }

    private static boolean isContainsAll(String param, Set<Character> paramsSet) {
        for (int i = 0; i<param.length(); i++) {
            if (!paramsSet.contains(param.charAt(i)))
                return false;
        }
        return true;
    }

    private static Annotation findOptFromArray(Annotation[] annotations) {
        for (Annotation anno : annotations) {
            if (anno.annotationType() == Opt.class) {
                return anno;
            }
        }
        return null;
    }

    private static Object resolveArgument(Object value, Class<?> classType, Type genericType) {
        try {
            if (value.equals(PLACEHOLDER))
                return true;
            else
                return TransformFactory.resolveArgument(value, classType, genericType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("格式解析时异常: " + e.getMessage());
        }
    }

    // map 中包含了所需的参数，则从map中移除此key
    // return -> true: 没找到这个参数； false: 已找到，并进行了设置
    private static boolean getAndRemove(List<Object> arg, Set<String> keySet,
                                        Class<?> classType, Type genericType, Map<?, Object> map) {
        boolean found = false;
        for (String key : keySet) {
            if (map.containsKey(String.valueOf(key))) {
                arg.add(resolveArgument(map.get(key), classType, genericType));
                found = true;
                break;
            }
        }

        if (!found) {
            arg.add(TransformFactory.getDefVal(classType));
        } else {
            for (String key : keySet) {
                map.remove(key);
            }
        }

        return !found;
    }

    // ------------------------------------POJO--------------------------------------------

    public static class ResultWrapper {

        protected final boolean success;
        protected final Object[] args;
        protected final Exception ex;

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
