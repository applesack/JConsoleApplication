package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.NoStatus;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.util.InvokeProxy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 当你想要的很简单的命令参数输入，请将@Cmd注解的parser标记为sub
 * <p><pre>注意: 不处理集合，不处理泛型
 * 解析格式为:
 *      参数名 参数值 ...
 * 其中参数值不需要以’-‘做为前缀
 * 举个例子，这是系统预设的一个命令，这里拿这个命令做为演示: </pre></p>
 * <pre>{@code @Cmd(tag = SYS_TAG, parser = "sub")
 * public void task(@Opt(value = 's', fullName = "name", dftVal = "*") String taskName,
 *                  @Opt(value = 'n', fullName = "size", dftVal = "-1") int size,
 *                  @Opt(value = 'c', fullName = "clear", dftVal = "*") String clear) {
 * }}</pre>
 *
 * 你可以这样输入命令行
 * <pre>
 * task clear
 * task name nameOfTask size 9
 * task nameOfTask 9
 * </pre>
 * @see DftParameterParser 参照默认实现
 *
 * @author flutterdash@qq.com
 * @since 2021/2/6 14:47
 */
@NoStatus
public final class SubParameterParser implements NameableParameterParser {
    /** singleton */
    protected static final SubParameterParser INSTANCE = new SubParameterParser();

    @Override
    public String name() {
        return "sub";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, List<String> args) {
        // 提取基本信息
        Optional<Opt>[] paramOptAnnoList = meta.optionals; // 参数全称集合
        List<Object> methodArgs = new ArrayList<>();       // 待返回的参数列表
        Map<String, String> kvPairs = new HashMap<>();     // 命令行中的参数键值对
        List<String> remainList = parseParameters(args, paramOptAnnoList, kvPairs); // 命令行移除了键值对后剩余的内容

        Class<?>[] methodArgTypes = meta.parameterTypes; // 方法参数类型
        Optional<Opt>[] optionals = meta.optionals;      // 方法中的注解数组，每个方法参数对应一个注解元素
        List<SimpleWildcardArgument> wildcardArguments = new ArrayList<>(); // 当一个方法参数是缺省的，则被加入道这个集合
        // 遍历方法的所有参数
        int size = meta.size;
        for (int i = 0; i<size; i++) {
            Class<?> curParamType = methodArgTypes[i]; // 当前参数类型
            Optional<Opt> optOptional = optionals[i];  // 当前参数对应的注解
            if (optOptional.isPresent()) { // 含有 @Opt 注解时
                Opt option = optOptional.get();
                String paramName = getNameStrategy(option);
                if (kvPairs.containsKey(paramName)) {
                    String value = kvPairs.get(paramName);
                    Object obj = exTransform(value, curParamType);
                    if (obj instanceof Exception)
                        return ParameterWrapper.fail(
                                new ParameterResolveException("参数解析时异常, 类型:" + curParamType.getName(),
                                        (Exception) obj).setErrorInfo(ErrorCode.NONSUPPORT_TYPE));
                    else
                        methodArgs.add(obj);
                } else { // 当前参数不在参数键值对中时
                    if (!option.dftVal().equals("")) {
                        Object obj = exTransform(option.dftVal(), curParamType);
                        if (obj instanceof Exception)
                            return ParameterWrapper.fail(
                                    new ParameterResolveException("参数解析时异常, 类型:" + curParamType.getName(),
                                            (Exception) obj).setErrorInfo(ErrorCode.NONSUPPORT_TYPE));
                        else
                            methodArgs.add(obj);
                    } else {
                        if (option.required())
                            return ParameterWrapper.fail(
                                    new ParameterResolveException("缺少必选参数 `" + option.fullName() + "`")
                                            .setErrorInfo(ErrorCode.LACK_REQUIRED_PARAMETERS));
                        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(curParamType);
                        if (presetObjOptional.isPresent()) {
                            methodArgs.add(presetObjOptional.get());
                        } else {
                            methodArgs.add(TransformFactory.getDefVal(curParamType));
                            wildcardArguments.add(new SimpleWildcardArgument(i, curParamType));
                        }
                    }
                }
            } else { // 没有 @Opt 注解时，先放置一个默认值，然后在这个位置做一个标记
                Optional<Object> presetObjOptional = TransformFactory.getPresetVal(curParamType);
                if (presetObjOptional.isPresent()) {
                    methodArgs.add(presetObjOptional.get());
                } else {
                    methodArgs.add(TransformFactory.getDefVal(curParamType));
                    wildcardArguments.add(new SimpleWildcardArgument(i, curParamType));
                }
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
