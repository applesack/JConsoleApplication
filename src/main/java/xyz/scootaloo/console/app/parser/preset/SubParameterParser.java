package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.parser.MethodMeta.Context;
import xyz.scootaloo.console.app.parser.MethodMeta.CurrentParamInfo;
import xyz.scootaloo.console.app.parser.MethodMeta.LackMark;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Sub
 *
 * @author flutterdash@qq.com
 * @since 2021/2/6 14:47
 */
@Stateless
public final class SubParameterParser implements NameableParameterParser {
    /** singleton */
    protected static final SubParameterParser INSTANCE = new SubParameterParser();

    @Override
    public String name() {
        return "sub";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) {
        // 提取基本信息
        Context<Object> state = Context.getInstance(meta, args, SubParameterParser::parseParameters);

        // 按顺序遍历所有方法参数.  current: 当前的方法参数
        for (CurrentParamInfo current : meta) {
            if (current.hasOptAnno()) {
                doResolveIfAnnoExist(state, current);
                if (state.hasException())
                    return ParameterWrapper.fail(state.getException());
            } else {
                setProbableValue(state, current);
            }
        }

        // 尝试将命令行中剩余的内容解析到缺省的参数位置上
        if (state.hasLackMark()) {
            remainItemsMapToArgument(state);
        }

        return ParameterWrapper.success(state.getMethodArgs());
    }

    private static void remainItemsMapToArgument(Context<Object> state) {
        List<String> remainList = state.getRemainList();
        for (LackMark lacks : state.getLackMarks()) {
            if (!remainList.isEmpty()) {
                String remains = remainList.remove(0);
                if (remains.isEmpty())
                    continue;
                Object obj = exTransform(remains, lacks.type());
                if (!(obj instanceof Exception)) {
                    state.getMethodArgs().set(lacks.index(), obj);
                }
            } else {
                break;
            }
        }
    }

    // 有注解时，处理参数解析
    private static void doResolveIfAnnoExist(Context<Object> state, CurrentParamInfo current) {
        Opt opt = current.getAnno();
        String paramName = getNameStrategy(opt);
        // 命令行参数中有当前方法参数的信息
        if (state.containParam(paramName)) {
            String value = state.getParamValue(paramName);
            Object parserResult = exTransform(value, current.getParamType());
            if (parserResult instanceof Exception) {
                state.setException(ParameterParser
                        .createException("参数解析时异常, 类型:" +
                                current.getParamType().getName(),
                                (Throwable) parserResult, ErrorCode.NONSUPPORT_TYPE));
            } else {
                state.addMethodArgument(parserResult);
            }
        }
        // 命令行参数中没有此方法参数
        else {
            // 使用默认值
            if (current.hasDefaultValue()) {
                Object parserResult = exTransform(current.getDefaultValue(), current.getParamType());
                if (parserResult instanceof Exception) {
                    state.setException(ParameterParser
                            .createException("参数解析时异常, 类型:" + current.getParamType().getName(),
                            (Throwable) parserResult, ErrorCode.NONSUPPORT_TYPE));
                } else {
                    state.addMethodArgument(parserResult);
                    setProbableValue(state, current);
                }
            } else {
                if (current.isRequired()) {
                    state.setException(ParameterParser.createException("缺少必选参数 `" +
                            getNameStrategy(current.getAnno()) + "`"));
                } else {
                    setProbableValue(state, current);
                }
            }
        }
    }

    // 在数值空缺的时候尝试放置一个预设值，并在这个位置作一个标记
    private static void setProbableValue(Context<Object> state,
                                         CurrentParamInfo current) {

        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.getParamType());
        Object fillObj;
        if (presetObjOptional.isPresent()) {
            fillObj = presetObjOptional.get();
        } else {
            fillObj = TransformFactory.getDefVal(current.getParamType());
            state.addLackMark(new LackMark(current.index(), current.getParamType()));
        }
        if (fillObj != null)
            state.getMethodArgs().set(current.index(), fillObj);
    }

    /**
     * 将字符串解析成某种类型时，对其过程中抛出的异常进行处理
     * @param source 来源 命令行中的字符串
     * @param type 目标 方法参数中的类型
     * @return 解析成功 返回对象； 解析失败 返回异常对象
     */
    private static Object exTransform(String source, Class<?> type) {
        try {
            return TransformFactory.simpleTrans(source, type);
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * 将命令行中与方法参数中注解描述相符的参数项提取出来，并从原命令行中删除该项
     * @param state 解析状态
     * @param args 命令行参数
     * @return 命令行中剩余的内容
     */

    private static List<String> parseParameters(Context<Object> state, String args) {
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
    private static Set<String> getParamKeySetForAnnoList(Optional<Opt>[] annoList) {
        return Arrays.stream(annoList)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(SubParameterParser::getNameStrategy)
                .collect(Collectors.toSet());
    }

    // 选取参数名的策略，优先选取 fullName， 其次选取 value 值
    private static String getNameStrategy(Opt opt) {
        if (opt.fullName().isEmpty())
            return String.valueOf(opt.value());
        else
            return opt.fullName();
    }

    @Override
    public String toString() {
        return getParserString();
    }

}
