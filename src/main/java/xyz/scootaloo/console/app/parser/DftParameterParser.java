package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.parser.MethodMeta.Context;
import xyz.scootaloo.console.app.parser.MethodMeta.CurrentParamInfo;
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
public final class DftParameterParser extends FillParamInOrder<Object> implements NameableParameterParser {
    // 临时占位符
    private static final String PLACEHOLDER = "*";
    protected static final DftParameterParser INSTANCE = new DftParameterParser();

    @Override
    protected Context<Object> createContext(MethodMeta meta, String args) {
        return Context.getInstance(meta, args, this::loadArgumentFromCmdline);
    }

    @Override
    protected void doResolveIfAnnoExist(Context<Object> state, CurrentParamInfo current) {
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
                state.setException(createException("缺少必要参数: " + shortKeyName + " | " + fullKeyName));
            else
                addLackMark(state, current);
        }
    }

    @Override
    protected void doResolveIfAnnoMissing(Context<Object> state, CurrentParamInfo current) {
        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.getParamType());
        if (presetObjOptional.isPresent()) {
            state.addMethodArgument(presetObjOptional.get());
            return;
        }

        if (state.getRemainList().isEmpty()) {
            state.setException(
                    createException("命令不完整，在第" + (current.index() + 1) + "个参数，" + "参数类型: "));
            return;
        }

        try {
            state.addMethodArgument(parsingParam(state.getRemainList().remove(0),
                    current.getParamType(), current.getGenericType()));
        } catch (Exception e) {
            state.setException(createException("参数解析异常", e, ErrorCode.RESOLVE_ERROR));
        }
    }

    // 从命令行中取出命令参数和值
    private List<String> loadArgumentFromCmdline(Context<Object> state, String args) {
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
    @Override
    protected Object parsingParam(Object value, Class<?> classType, Type genericType) {
        if ((classType == boolean.class || classType == Boolean.class) && value.equals(PLACEHOLDER))
            return true;
        return super.parsingParam(value, classType, genericType);
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
    public String toString() {
        return getParserString();
    }

}
