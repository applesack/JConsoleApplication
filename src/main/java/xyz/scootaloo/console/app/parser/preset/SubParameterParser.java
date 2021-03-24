package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.parser.MethodMeta.CurrentParamType;
import xyz.scootaloo.console.app.support.InvokeProxy;

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
        List<String> cmdArgsItems = Actuator.splitCommandArgsBySpace(args);
        Optional<Opt>[] paramOptAnnoList = meta.optionals; // 参数全称集合
        List<Object> methodArgs = new ArrayList<>();       // 待返回的参数列表
        Map<String, String> kvPairs = new HashMap<>();     // 命令行中的参数键值对
        List<String> remainList = parseParameters(cmdArgsItems, paramOptAnnoList, kvPairs); // 命令行移除了键值对后剩余的内容

        // 遍历方法的所有参数
        List<SimpleWildcardArgument> wildcardArguments = new ArrayList<>(); // 当一个方法参数是缺省的，则被加入道这个集合
        for (CurrentParamType current : meta) {
            if (current.optionalOpt.isPresent()) { // 含有 @Opt 注解时
                Opt option = current.optionalOpt.get();
                String paramName = getNameStrategy(option);
                if (kvPairs.containsKey(paramName)) {
                    String value = kvPairs.get(paramName);
                    Object obj = exTransform(value, current.paramType);
                    if (obj instanceof Exception)
                        return ParameterWrapper.fail(
                                new ParameterResolveException("参数解析时异常, 类型:" + current.paramType.getName(),
                                        (Exception) obj).setErrorInfo(ErrorCode.NONSUPPORT_TYPE));
                    else
                        methodArgs.add(obj);
                } else { // 当前参数不在参数键值对中时
                    if (!option.dftVal().equals("")) {
                        Object obj = exTransform(option.dftVal(), current.paramType);
                        if (obj instanceof Exception)
                            return ParameterWrapper.fail(
                                    new ParameterResolveException("参数解析时异常, 类型:" + current.paramType.getName(),
                                            (Exception) obj).setErrorInfo(ErrorCode.NONSUPPORT_TYPE));
                        else
                            methodArgs.add(obj);
                    } else {
                        if (option.required())
                            return ParameterWrapper.fail(
                                    new ParameterResolveException("缺少必选参数 `" + option.fullName() + "`")
                                            .setErrorInfo(ErrorCode.LACK_REQUIRED_PARAMETERS));
                        setProbableValue(methodArgs, wildcardArguments, current);
                    }
                }
            } else { // 没有 @Opt 注解时，先放置一个默认值，然后在这个位置做一个标记
                setProbableValue(methodArgs, wildcardArguments, current);
            }
        }

        // 尝试将命令行中剩余的内容解析到缺省的参数位置上
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

    private void setProbableValue(List<Object> methodArgs, List<SimpleWildcardArgument> wildcardArguments, CurrentParamType current) {
        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.paramType);
        if (presetObjOptional.isPresent()) {
            methodArgs.add(presetObjOptional.get());
        } else {
            methodArgs.add(TransformFactory.getDefVal(current.paramType));
            wildcardArguments.add(new SimpleWildcardArgument(current.index, current.paramType));
        }
    }

    /**
     * 将字符串解析成某种类型时，对其过程中抛出的异常进行处理
     * @param source 来源 命令行中的字符串
     * @param type 目标 方法参数中的类型
     * @return 解析成功 返回对象； 解析失败 返回异常对象
     */
    private Object exTransform(String source, Class<?> type) {
        final Exception[] exContainer = {null};
        Object res = InvokeProxy.fun(TransformFactory::simpleTrans)
                        .addHandle((ex) -> exContainer[0] = ex)
                        .call(source, type);
        return exContainer[0] == null ? res : exContainer[0];
    }

    /**
     * 将命令行中与方法参数中注解描述相符的参数项提取出来，并从原命令行中删除该项
     * @param commandArgItems 命令行
     * @param annoList 方法参数上的注解列表
     * @param kvPairs 提供一个容器用于接收从命令行中提取出来的键值对
     * @return 命令行中剩余的内容
     */
    private List<String> parseParameters(List<String> commandArgItems, Optional<Opt>[] annoList,
                                         Map<String, String> kvPairs) {
        Set<String> paramSet = getParamKeySetForAnnoList(annoList);
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

    /**
     * 优先选取 Opt 注解的 fullName 属性，假如没有指定 fullName，则使用 value 值
     * @param annoList 方法参数上的注解
     * @return 收集到的参数名集合
     */
    private Set<String> getParamKeySetForAnnoList(Optional<Opt>[] annoList) {
        return Arrays.stream(annoList)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getNameStrategy)
                .collect(Collectors.toSet());
    }

    // 选取参数名的策略，优先选取 fullName， 其次选取 value 值
    private String getNameStrategy(Opt opt) {
        if (opt.fullName().isEmpty())
            return String.valueOf(opt.value());
        else
            return opt.fullName();
    }

    @Override
    public String toString() {
        return getParserString();
    }

    // 缺省参数标记
    private static class SimpleWildcardArgument {
        private final int idx;
        private final Class<?> type;
        public SimpleWildcardArgument(int index, Class<?> type) {
            this.idx = index;
            this.type = type;
        }
    }

}
