package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.parser.MethodMeta.Context;
import xyz.scootaloo.console.app.parser.MethodMeta.CurrentParamInfo;
import xyz.scootaloo.console.app.parser.MethodMeta.LackMark;
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
@Stateless
public final class DftParameterParser implements NameableParameterParser {
    // 临时占位符
    private static final String PLACEHOLDER = "*";

    /**
     * *解析逻辑实现的入口*
     * @param meta method包装
     * @param cmdArgs 调用此方法使用的命令参数
     * @return 包装的结果
     */
    public static ResultWrapper transform(MethodMeta meta, String cmdArgs) {
        if (meta.size == 0)
            return ParameterWrapper.success(null);
        Context<Object> state = Context
                .getInstance(meta, cmdArgs, DftParameterParser::loadArgumentFromCmdline);
        for (CurrentParamInfo current : meta) {
            if (current.hasOptAnno()) {
                doResolveIfAnnoExist(state, current);
                if (state.hasException())
                    return ParameterWrapper.fail(state.getException());
            } else {
                doResolveIfAnnoMissing(state, current);
                if (state.hasLackMark())
                    return ParameterWrapper.fail(state.getException());
            }
        }

        if (state.hasLackMark()) {
            remainItemsMapToArgument(state);
        }

        return ParameterWrapper.success(state.getMethodArgs());
    }

    private static void remainItemsMapToArgument(Context<Object> state) {
        List<String> remainList = state.getRemainList();
        for (LackMark mark : state.getLackMarks()) {
            // 从剩余的命令参数列表中，依次填充到这些未选中的方法参数上
            String value;
            if (!remainList.isEmpty()) {
                if (mark.isJoint()) {
                    List<String> jointList = new ArrayList<>();
                    while (!remainList.isEmpty()) {
                        jointList.add(remainList.remove(0));
                    }
                    value = String.join(" ", jointList);
                } else {
                    value = remainList.remove(0);
                }
                Object parsingResult = parsingParam(value, mark.type(), mark.generic());
                if (parsingResult instanceof Exception)
                    continue;
                state.getMethodArgs().set(mark.index(), parsingResult);
            }
        }
    }

    private static void doResolveIfAnnoExist(Context<Object> state, CurrentParamInfo current) {
        Opt opt = current.getAnno();
        String shortKeyName = String.valueOf(opt.value());
        String fullKeyName = opt.fullName();
        if (state.containParam(shortKeyName) || state.containParam(fullKeyName)) {
            if (!fullKeyName.isEmpty() && state.containParam(fullKeyName)) {
                mapToArgument(state, current, fullKeyName);
            } else {
                mapToArgument(state, current, shortKeyName);
            }
        } else {
            if (current.isRequired())
                state.setException(
                        ParameterParser.createException("缺少必要参数: " + shortKeyName + " | " + fullKeyName));
            else
                addLackMark(state, current);
        }
    }

    private static void doResolveIfAnnoMissing(Context<Object> state, CurrentParamInfo current) {
        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.getParamType());
        if (presetObjOptional.isPresent()) {
            state.addMethodArgument(presetObjOptional.get());
            return;
        }

        if (state.getRemainList().isEmpty()) {
            state.setException(ParameterParser
                    .createException("命令不完整，在第" + (current.index() + 1) + "个参数，" + "参数类型: "));
            return;
        }

        try {
            state.addMethodArgument(parsingParam(state.getRemainList().remove(0),
                    current.getParamType(), current.getGenericType()));
        } catch (Exception e) {
            state.setException(ParameterParser.createException("参数解析异常", e, ErrorCode.RESOLVE_ERROR));
        }
    }

    // 从命令行中取出命令参数和值
    private static List<String> loadArgumentFromCmdline(Context<Object> state, String args) {
        List<String> argItems = Actuator.splitCommandArgsBySpace(args);
        MethodMeta meta = state.meta();
        LinkedList<String> retainList = new LinkedList<>();
        for (int i = 0; i<argItems.size(); i++) {
            String curSegment = argItems.get(i);
            if (StringUtils.isNumber(curSegment) || !curSegment.startsWith("-")) {
                retainList.addLast(curSegment);
                continue;
            }

            int prefixCount = curSegment.startsWith("--") ? 2 : 1;
            curSegment = curSegment.substring(prefixCount);
            if (curSegment.isEmpty())
                continue;
            if (meta.jointMarkSet.contains(curSegment)) {
                List<String> jointList = new ArrayList<>();
                for (i += 1; i<argItems.size(); i++) {
                    jointList.add(argItems.get(i));
                }
                if (!jointList.isEmpty())
                    state.getKVPairs().put(curSegment, String.join(" ", jointList));
                continue;
            }

            String nextSeg = PLACEHOLDER;
            if (curSegment.length() > 1 && isContainsAll(curSegment, meta.optCharSet)) {
                for (char shortParam : curSegment.toCharArray()) {
                    state.getKVPairs().put(String.valueOf(shortParam), "true");
                }
                continue;
            }
            if (i < argItems.size() - 1 && !argItems.get(i + 1).startsWith("-")) {
                nextSeg = argItems.get(i + 1);
                i++;
            }
            state.getKVPairs().put(curSegment, nextSeg);
        }
        return retainList;
    }

    // 将一个对象根据类型解析成另外一个对象
    private static Object parsingParam(Object value, Class<?> classType, Type genericType) {
        if ((classType == boolean.class || classType == Boolean.class) && value.equals(PLACEHOLDER))
            return true;
        else {
            try {
                return TransformFactory.parsingParam(value, classType, genericType);
            } catch (Exception e) {
                return e;
            }
        }
    }

    private static void mapToArgument(Context<Object> state, CurrentParamInfo current, String key) {
        if (key == null) {
            addLackMark(state, current);
            return;
        }
        Map<String, String> map = state.getKVPairs();
        Object parsingResult = parsingParam(map.get(key), current.getParamType(), current.getGenericType());
        if (parsingResult instanceof Exception)
            state.setException(ParameterParser
                    .createException("参数解析错误", (Throwable) parsingResult, ErrorCode.RESOLVE_ERROR));
        else
            state.addMethodArgument(parsingResult);
    }

    // 放置默认值, 并增加标记
    private static void addLackMark(Context<Object> state, CurrentParamInfo current) {
        // 预设值
        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.getParamType());
        if (presetObjOptional.isPresent()) {
            state.addMethodArgument(presetObjOptional.get());
        } else {
            // 默认值
            if (current.hasOptAnno() && !current.getAnno().dftVal().isEmpty()) {
                Object parsingResult = parsingParam(
                        current.getAnno().dftVal(), current.getParamType(), current.getGenericType());
                if (parsingResult instanceof Exception) {
                    state.setException(
                            ParameterParser.createException("参数解析异常",
                                    (Throwable) parsingResult, ErrorCode.RESOLVE_ERROR));
                } else {
                    state.addMethodArgument(parsingResult);
                }
            } else {
                state.addMethodArgument(TransformFactory.getDefVal(current.getParamType()));
            }
        }
        state.addLackMark(new LackMark(
                current.index(), current.getParamType(), current.getGenericType(), current.isJoint()));
    }

    // 检查一个命令参数是否由多个可选项参数构成
    private static boolean isContainsAll(String param, Set<Character> paramsSet) {
        for (int i = 0; i<param.length(); i++) {
            if (!paramsSet.contains(param.charAt(i)))
                return false;
        }
        return true;
    }

    @Override
    public String name() {
        return "*";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) {
        return transform(meta, args);
    }

    @Override
    public String toString() {
        return getParserString();
    }

}
