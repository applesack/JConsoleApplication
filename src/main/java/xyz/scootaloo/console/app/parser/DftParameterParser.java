package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.NoStatus;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 系统默认参数解析器实现
 * <pre>支持:
 *      按照顺序填充方法参数
 *      按照方法参数的注解来填充参数
 *      参数默认值
 *      简化布尔类型参数的写法</pre>
 * <p>其他内容请参考 README.md </p>
 * @author flutterdash@qq.com
 * @since 2020/12/29 11:21
 */
@NoStatus
public final class DftParameterParser implements NameableParameterParser {
    // 临时占位符
    private static final String PLACEHOLDER = "*";

    /**
     * *解析逻辑实现的入口*
     * @param meta method包装
     * @param cmdline 调用此方法使用的命令参数
     * @return 包装的结果
     */
    public static ResultWrapper transform(MethodMeta meta, List<String> cmdline) throws Exception {
        if (meta.size == 0)
            return ParameterWrapper.success(null);
        Class<?>[] argTypes = meta.parameterTypes;       // 参数类型数组
        Type[] genericTypes = meta.genericTypes;         // 参数泛型类型数组
        Optional<Opt>[] optionals = meta.optionals;      // 参数注解数组
        List<Object> args = new ArrayList<>();           // 最终可供Method对象invoke的参数
        Set<String> jointMarkSet = meta.jointMarkSet;    // 连接标记
        Set<Character> shortParamsSet = meta.optCharSet; // 由注解中的简写命令参数名构成的集

        List<WildcardArgument> wildcardArguments = new ArrayList<>(); // 未提供参数的位置
        // key=命令参数名 value=命令参数值
        Map<String, Object> optMap = new HashMap<>(); // 参数map       // 命令参数中由'-'做前缀的参数以及值
        List<String> remainList = loadArgumentFromCmdline(cmdline, optMap, shortParamsSet, jointMarkSet);

        for (int i = 0; i<argTypes.length; i++) {
            Optional<Opt> curAnno = optionals[i];
            Class<?> curArgType = argTypes[i];
            // 当前这个参数没有注解，尝试将一个无名参数解析到这里
            if (!curAnno.isPresent()) {
                Optional<Object> presetObjOptional = TransformFactory.getPresetVal(curArgType);
                if (presetObjOptional.isPresent()) {
                    args.add(presetObjOptional.get());
                    continue;
                }
                if (remainList.isEmpty())
                    return ParameterWrapper.fail(
                            new ParameterResolveException("命令不完整，在第" + (i + 1) + "个参数，" + "参数类型: ")
                                .setErrorInfo(ErrorCode.LACK_PARAMETER));
                args.add(resolveArgument(remainList.remove(0), argTypes[i], genericTypes[i]));
                continue;
            }

            // 只处理 @Opt 注解
            Opt option = curAnno.get();
            Set<String> paramsSet = new LinkedHashSet<>();
            paramsSet.add(String.valueOf(option.value()));
            if (!option.fullName().isEmpty())
                paramsSet.add(option.fullName());

            // 没有此参数
            if (getAndRemove(args, paramsSet, curArgType, genericTypes[i], optMap)) {
                if (option.required())
                    return ParameterWrapper.fail(
                            new ParameterResolveException("缺少必要的参数: -" + option.value())
                                .setErrorInfo(ErrorCode.LACK_REQUIRED_PARAMETERS));

                // 给这个位置的参数做一个标记，假如处理完还有多余的参数就填补到这个位置来
                wildcardArguments.add(new WildcardArgument(i, curArgType, genericTypes[i], option.joint()));
                // 在getAndRemove()方法中已经处理了类型默认值的情况，这里处理用户给定的自定义默认值
                if (!option.dftVal().equals("")) {
                    args.set(args.size() - 1, TransformFactory
                            .simpleTrans(option.dftVal(), curArgType));
                }
            }
        }

        // 处理多余的命令参数
        if (!wildcardArguments.isEmpty()) {
            for (WildcardArgument wildcardArgument : wildcardArguments) {
                // 从剩余的命令参数列表中，依次填充到这些未选中的方法参数上
                String res;
                if (!remainList.isEmpty()) {
                    if (wildcardArgument.joint) {
                        List<String> jointList = new ArrayList<>();
                        while (!remainList.isEmpty()) {
                            jointList.add(remainList.remove(0));
                        }
                        res = String.join(" ", jointList);
                    } else {
                        res = remainList.remove(0);
                    }
                    args.set(wildcardArgument.idx,
                            resolveArgument(res, wildcardArgument.type, wildcardArgument.generic));
                }
            }
        }

        return ParameterWrapper.success(args);
    }

    // 从命令行中取出命令参数和值
    private static List<String> loadArgumentFromCmdline(List<String> cmdline,
                                                        Map<String, Object> optMap,
                                                        Set<Character> shortParamsSet,
                                                        Set<String> jointMarkSet) {
        LinkedList<String> retainList = new LinkedList<>();
        for (int i = 0; i<cmdline.size(); i++) {
            String curSegment = cmdline.get(i);
            if (StringUtils.isNumber(curSegment) || !curSegment.startsWith("-")) {
                retainList.addLast(curSegment);
                continue;
            }

            int prefixCount = curSegment.startsWith("--") ? 2 : 1;
            curSegment = curSegment.substring(prefixCount);
            if (curSegment.isEmpty())
                continue;
            if (jointMarkSet.contains(curSegment)) {
                List<String> jointList = new ArrayList<>();
                for (i += 1; i<cmdline.size(); i++) {
                    jointList.add(cmdline.get(i));
                }
                if (!jointList.isEmpty())
                    optMap.put(curSegment, String.join(" ", jointList));
                continue;
            }
            String nextSeg = PLACEHOLDER;
            if (curSegment.length() > 1 && isContainsAll(curSegment, shortParamsSet)) {
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

    // 检查一个命令参数是否由多个可选项参数构成
    private static boolean isContainsAll(String param, Set<Character> paramsSet) {
        for (int i = 0; i<param.length(); i++) {
            if (!paramsSet.contains(param.charAt(i)))
                return false;
        }
        return true;
    }

    // 将一个对象根据类型解析成另外一个对象
    private static Object resolveArgument(Object value, Class<?> classType, Type genericType) throws Exception {
        if ((classType == boolean.class || classType == Boolean.class) && value.equals(PLACEHOLDER))
            return true;
        else
            return TransformFactory.resolveArgument(value, classType, genericType);
    }

    // map 中包含了所需的参数，则从map中移除此key
    // return -> true: 没找到这个参数； false: 已找到，并进行了设置
    private static boolean getAndRemove(List<Object> args, Set<String> keySet,
                                        Class<?> classType, Type genericType,
                                        Map<?, Object> map) throws Exception {
        boolean found = false;
        for (String key : keySet) {
            if (map.containsKey(String.valueOf(key))) {
                args.add(resolveArgument(map.get(key), classType, genericType));
                found = true;
                break;
            }
        }

        if (!found) {
            Optional<Object> presetObjOptional = TransformFactory.getPresetVal(classType);
            if (presetObjOptional.isPresent()) {
                args.add(presetObjOptional.get());
                return false;
            } else {
                args.add(TransformFactory.getDefVal(classType));
            }
        } else {
            for (String key : keySet) {
                map.remove(key);
            }
        }

        return !found;
    }

    @Override
    public String name() {
        return "*";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, List<String> args) throws Exception {
        return transform(meta, args);
    }

    @Override
    public String toString() {
        return getParserString();
    }

    // ------------------------------------POJO--------------------------------------------

    // 用于处理可选参数缺省的情况
    private static class WildcardArgument {

        final int idx;       // 缺省参数位于参数数组的下标
        final Class<?> type; // 对应方法参数的类型
        final Type generic;  // 对应方法参数的泛型类型
        final boolean joint; // 是否需要拼接参数

        public WildcardArgument(int idx, Class<?> type, Type generic, boolean joint) {
            this.idx = idx;
            this.type = type;
            this.generic = generic;
            this.joint = joint;
        }

    }

}
