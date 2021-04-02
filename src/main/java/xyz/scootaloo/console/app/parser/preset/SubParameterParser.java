package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.parser.MethodMeta.Context;
import xyz.scootaloo.console.app.parser.MethodMeta.CurrentParamInfo;

import java.util.*;
import java.util.stream.Collectors;

/**
 * sub
 *
 * @author flutterdash@qq.com
 * @since 2021/2/6 14:47
 */
@Stateless
public final class SubParameterParser extends FillParamInOrder<Object> implements NameableParameterParser {
    /** singleton */
    protected static final SubParameterParser INSTANCE = new SubParameterParser();

    @Override
    protected Context<Object> createContext(MethodMeta meta, String args) {
        return Context.getInstance(meta, args, this::parseParameters);
    }

    // 有注解时，处理参数解析
    @Override
    protected void doResolveIfAnnoExist(Context<Object> state, CurrentParamInfo current) {
        Opt opt = current.getAnno();
        String paramName = getNameStrategy(opt);
        // 命令行参数中有当前方法参数的信息
        if (state.containParam(paramName)) {
            String value = state.getParam(paramName);
            Object parserResult = parsingParam(value, current.getParamType());
            if (parserResult instanceof Exception) {
                state.setException(
                        createException("参数解析时异常, 类型:" + current.getParamType().getName(),
                                (Throwable) parserResult, ErrorCode.NONSUPPORT_TYPE));
            } else {
                state.addMethodArgument(parserResult);
            }
        }
        // 命令行参数中没有此方法参数
        else {
            addLackMark(state, current);
        }
    }

    /**
     * 将命令行中与方法参数中注解描述相符的参数项提取出来，并从原命令行中删除该项
     * @param state 解析状态
     * @param args 命令行参数
     * @return 命令行中剩余的内容
     */
    private List<String> parseParameters(Context<Object> state, String args) {
        List<String> cmdArgsItems = Actuator.splitCommandArgsBySpace(args);  // 用空格分隔的命令参数列表
        Map<String, String> kvPairs = state.getKVPairs();
        Set<String> paramSet = getParamKeySetForAnnoList(state.getOptionals());
        List<String> remainList = new ArrayList<>();
        int size = cmdArgsItems.size();
        for (int i = 0; i<size; i++) {
            String key = cmdArgsItems.get(i);
            if (paramSet.contains(key)) {
                if (i+1 < size && !paramSet.contains(cmdArgsItems.get(i+1))) {
                    kvPairs.put(key, cmdArgsItems.get(i+1));
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

    /**
     * 优先选取 Opt 注解的 fullName 属性，假如没有指定 fullName，则使用 value 值
     * @param annoList 方法参数上的注解
     * @return 收集到的参数名集合
     */
    private Set<String> getParamKeySetForAnnoList(Optional<Opt>[] annoList) {
        return Arrays.stream(annoList)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FillParamInOrder::getNameStrategy)
                .collect(Collectors.toSet());
    }

    @Override
    public String name() {
        return "sub";
    }

    @Override
    public String toString() {
        return getParserString();
    }

}
