package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.parser.NameableParameterParser;
import xyz.scootaloo.console.app.parser.ParameterWrapper;
import xyz.scootaloo.console.app.parser.TransformFactory;
import xyz.scootaloo.console.app.parser.Wrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 当你想要的很简单的命令参数输入，请将@Cmd注解的parser标记为sub
 * 注意: 不处理集合，不处理泛型，也不处理@Opt注解的value
 * 解析格式为:
 *      参数名 参数值 ...
 * 其中参数值不需要以’-‘做为前缀
 * 举个例子，这是系统预设的一个命令，拿这个命令做为演示
 *-    @Cmd(tag = SYS_TAG, parser = "sub")
 *     public void task(@Opt(value = 'c', fullName = "clear") boolean clear,
 *-                     @Opt(value = 's', fullName = "name") String taskName,
 *-                     @Opt(value = 'n', fullName = "size", dftVal = "-1") int size) {
 *     }
 *
 * 你可以这样输入命令行
 * task clear
 * task name nameOfTask size 9
 * task size 9
 *
 * @author flutterdash@qq.com
 * @since 2021/2/6 14:47
 */
public final class SubParameterParser implements NameableParameterParser {
    protected static final SubParameterParser INSTANCE = new SubParameterParser();

    @Override
    public String name() {
        return "sub";
    }

    @Override
    public Wrapper parse(Method method, List<String> args) {
        Set<String> paramSet = getParamSet(method);
        List<Object> methodArgs = new ArrayList<>();
        Map<String, String> kvPairs = new HashMap<>();
        List<String> remainList = parseParameters(args, paramSet, kvPairs);

        Class<?>[] methodArgTypes = method.getParameterTypes();
        Annotation[][] anno2DArr = method.getParameterAnnotations();
        List<SimpleWildcardArgument> wildcardArguments = new ArrayList<>();
        int size = method.getParameterCount();
        for (int i = 0; i<size; i++) {
            Class<?> curParamType = methodArgTypes[i];
            Annotation[] annoArr = anno2DArr[i];
            Optional<Opt> optOptional = findOption(annoArr);
            if (optOptional.isPresent()) {
                Opt option = optOptional.get();
                String paramName = option.fullName();
                if (kvPairs.containsKey(paramName)) {
                    String value = kvPairs.get(paramName);
                    Object obj = exTransform(value, curParamType);
                    if (obj instanceof Exception)
                        return ParameterWrapper.fail((Exception) obj);
                    else
                        methodArgs.add(obj);
                } else {
                    if (!option.dftVal().equals("")) {
                        Object obj = exTransform(option.dftVal(), curParamType);
                        if (obj instanceof Exception)
                            return ParameterWrapper.fail((Exception) obj);
                        else
                            methodArgs.add(obj);
                    } else {
                        methodArgs.add(TransformFactory.getDefVal(curParamType));
                    }
                    wildcardArguments.add(new SimpleWildcardArgument(i, curParamType));
                }
            } else {
                methodArgs.add(TransformFactory.getDefVal(curParamType));
                wildcardArguments.add(new SimpleWildcardArgument(i, curParamType));
            }
        }

        if (!wildcardArguments.isEmpty()) {
            for (SimpleWildcardArgument wildcardArgument : wildcardArguments) {
                if (!remainList.isEmpty()) {
                    Object obj = exTransform(remainList.remove(0), wildcardArgument.type);
                    if (!(obj instanceof Exception)) {
                        methodArgs.set(wildcardArgument.idx, obj);
                    }
                } else {
                    break;
                }
            }
        }

        return ParameterWrapper.success(methodArgs);
    }

    private Object exTransform(String source, Class<?> type) {
        Exception[] exContainer = {null};
        Object res = Console.dbEx(TransformFactory::simpleTrans, source, type, (ex) -> exContainer[0] = ex);
        return exContainer[0] == null ? res : exContainer[0];
    }

    private List<String> parseParameters(List<String> commandArgItems, Set<String> paramSet,
                                         Map<String, String> kvPairs) {
        List<String> remainList = new ArrayList<>();
        int size = commandArgItems.size();
        for (int i = 0; i<size; i++) {
            String key = commandArgItems.get(i);
            if (paramSet.contains(key)) {
                if (i+1 < size && !paramSet.contains(commandArgItems.get(i+1))) {
                    kvPairs.put(key, commandArgItems.get(i+1));
                    i++;
                } else {
                    kvPairs.put(key, "true");
                }
            } else {
                remainList.add(key);
            }
        }
        return remainList;
    }

    private Set<String> getParamSet(Method method) {
        Annotation[][] annotationArr = method.getParameterAnnotations();
        Set<String> paramSet = new LinkedHashSet<>();
        for (Annotation[] annoArr : annotationArr) {
            findOption(annoArr).ifPresent(option -> {
                if (!option.fullName().equals("")) {
                    paramSet.add(option.fullName());
                }
            });
        }
        return paramSet;
    }

    private Optional<Opt> findOption(Annotation[] annoArr) {
        for (Annotation anno : annoArr) {
            if (anno.annotationType() == Opt.class) {
                return Optional.of((Opt) anno);
            }
        }
        return Optional.empty();
    }

    private static class SimpleWildcardArgument {
        private final int idx;
        private final Class<?> type;
        public SimpleWildcardArgument(int index, Class<?> type) {
            this.idx = index;
            this.type = type;
        }
    }

}
