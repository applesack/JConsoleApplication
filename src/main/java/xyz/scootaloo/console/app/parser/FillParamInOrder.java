package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;
import xyz.scootaloo.console.app.error.ErrorCode;
import xyz.scootaloo.console.app.error.ParameterResolveException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 按顺序填充参数的解析器模板
 *
 * @author flutterdash@qq.com
 * @since 2021/4/1 16:56
 */
public abstract class FillParamInOrder<T> implements ParameterParser {
    private static final String WARNING_STRING = "使用模板需要重写此方法";

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) {
        return fillParamInOrder(meta, args);
    }

    /**
     * 填充逻辑:
     * 1. 根据输入创建一个上下文对象
     * 2. 遍历所有方法参数, 对有注解和无注解的方法参数做不同的处理
     *      2.1 如果能将一个值映射到方法参数上, 则映射
     *      2.1 这个值是缺失的, 则在此位置做一个标记, 等待后续处理
     *      2.3 映射过程中出现异常, 则直接将异常信息返回, 不执行后续操作
     * 3. 对缺省的方法参数进行处理
     * 4. 返回处理后的结果
     *
     * @param meta 方法的基础信息
     * @param args 命令行参数
     * @return 结构包装
     */
    protected final ResultWrapper fillParamInOrder(MethodMeta meta, String args) {
        // 假如方法参数长度为空, 直接返回
        if (meta.size == 0)
            return returnNothing();

        MethodMeta.Context<T> state = createContext(meta, args);

        for (MethodMeta.CurrentParamInfo current : meta) {
            if (current.hasOptAnno()) {
                doResolveIfAnnoExist(state, current);
            } else {
                doResolveIfAnnoMissing(state, current);
            }

            if (state.hasException())
                return ParameterWrapper.fail(state.getException());
        }

        if (state.hasLackMark())
            remainItemsMapToArgument(state);

        return ParameterWrapper.success(state.getMethodArgs());
    }

    protected ResultWrapper returnNothing() {
        return ParameterWrapper.success(null);
    }

    /**
     * 根据输入生成上下文对象
     * 从输入 args 中提取需要的信息
     * 保存到 state 管理的KVmap中
     *
     * @param meta 方法的基础信息
     * @param args 输入 命令行参数
     * @return 上下文对象
     */
    protected MethodMeta.Context<T> createContext(MethodMeta meta, String args) {
        throw new RuntimeException(WARNING_STRING);
    }

    /**
     * 处理有注解的方法参数
     *
     * @param state 上下文
     * @param current 当前参数的信息
     */
    protected void doResolveIfAnnoExist(MethodMeta.Context<T> state, MethodMeta.CurrentParamInfo current) {
    }

    /**
     * 处理没有注解的方法参数
     *
     * @param state 上下文
     * @param current 当前参数的信息
     */
    protected void doResolveIfAnnoMissing(MethodMeta.Context<T> state, MethodMeta.CurrentParamInfo current) {
    }

    /**
     * 处理缺省的方法参数
     *
     * @param state 上下文
     */
    protected void remainItemsMapToArgument(MethodMeta.Context<T> state) {
        List<String> remainList = state.getRemainList();
        for (MethodMeta.LackMark mark : state.getLackMarks()) {
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
                if (value != null && value.isEmpty())
                    continue;
                Object parsingResult = parsingParam(value, mark.type(), mark.generic());
                if (parsingResult instanceof Exception)
                    continue;
                state.getMethodArgs().set(mark.index(), parsingResult);
            } else {
                break;
            }
        }
    }

    /**
     * 这个方法会尝试在注解提供的信息中拿到一个默认值, 或者在预设的值中要找一个和当前方法参数匹配的值. 并映射到方法参数上
     * 假如没有在以上范围内找到这样一个值, 则在此位置放置空(数值类型=0, 引用类型=null, 布尔类型=false)
     * 并在上下文中做一个标记
     *
     * @param state 上下文
     * @param current 当前参数
     */
    protected void addLackMark(MethodMeta.Context<Object> state, MethodMeta.CurrentParamInfo current) {
        // 检查是否有与此参数同类型的预设值
        Optional<Object> presetObjOptional = TransformFactory.getPresetVal(current.getParamType());
        if (presetObjOptional.isPresent()) {
            state.addMethodArgument(presetObjOptional.get());
        } else {
            // 注解提供的默认值
            if (current.hasOptAnno() && current.hasDefaultValue()) {
                Object parsingResult = parsingParam(
                        current.getDefaultValue(), current.getParamType(), current.getGenericType());
                if (parsingResult instanceof Exception) {
                    state.setException(
                            createException("参数解析异常", (Throwable) parsingResult, ErrorCode.RESOLVE_ERROR));
                } else {
                    state.addMethodArgument(parsingResult);
                }
            } else {
                // 放置基础的默认值
                state.addMethodArgument(TransformFactory.getDefVal(current.getParamType()));
            }
        }
        // 在此位置标记
        state.addLackMark(new MethodMeta.LackMark(
                current.index(), current.getParamType(), current.getGenericType(), current.isJoint()));
    }

    /**
     * 尝试根据一个key,在上下文中找一个与之匹配的值.
     * 假如有, 则进行映射
     * 假如没有, 则在此位置做一个缺省标记
     *
     * @param state 上下文
     * @param current 当前参数
     * @param key 键
     */
    protected void mapToArgument(MethodMeta.Context<Object> state,
                                 MethodMeta.CurrentParamInfo current, String key) {
        if (key == null) {
            addLackMark(state, current);
            return;
        }
        Map<String, String> map = state.getKVPairs();
        // map.get(key) 可能返回null, 所以这里的parsingParam调用可能因此返回异常
        // 建议调用此方法前先检查上下文中是否存在与此键对应的值
        Object parsingResult = parsingParam(map.get(key), current.getParamType(), current.getGenericType());
        if (parsingResult instanceof Exception)
            state.setException(
                    createException("参数解析错误", (Throwable) parsingResult, ErrorCode.RESOLVE_ERROR));
        else
            state.addMethodArgument(parsingResult);
    }

    /**
     * 将一个值, 解析到指定的类型
     * 假如解析成功, 返回解析结果
     * 假如解析过程中遇到异常, 则把这个异常对象返回
     *
     * @param value 要被转换的值, 必须是字符串, 因为TransformFactory只设定了字符串的解析方式
     * @param classType 目标类型
     * @return 解析结果
     */
    protected Object parsingParam(Object value, Class<?> classType) {
        try {
            return TransformFactory.simpleTrans(value, classType);
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * 与上面一个方法描述类似, 不过这个方法额外有处理泛型的功能, 用于处理一些集合(Set, List, Array)
     *
     * @param value 要被转换的值, 必须是字符串, 因为TransformFactory只设定了字符串的解析方式
     * @param classType 目标类型
     * @param genericType 目标类型的泛型类型
     * @return 解析结果, 如果解析失败, 返回异常对象
     */
    protected Object parsingParam(Object value, Class<?> classType, Type genericType) {
        try {
            return TransformFactory.parsingParam(value, classType, genericType);
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * 创建一个控制台异常对象
     *
     * @param info 异常信息
     * @return 异常对象
     */
    public static ConsoleAppRuntimeException createException(String info) {
        return createException(info, null, ErrorCode.LACK_REQUIRED_PARAMETERS);
    }

    /**
     * 创建一个控制台异常对象
     *
     * @param info 异常信息
     * @param cause 异常来源
     * @param code 错误代码
     * @return 异常对象
     */
    public static ConsoleAppRuntimeException createException(String info, Throwable cause, ErrorCode code) {
        return new ParameterResolveException(info, cause).setErrorInfo(code);
    }

    /**
     * 按照一定的策略获取参数名
     * 优先返回 fullName, 只有fullName为空, 才返回value
     *
     * @param opt 注解
     * @return 参数名
     */
    public static String getNameStrategy(Opt opt) {
        if (opt.fullName().isEmpty())
            return String.valueOf(opt.value());
        else
            return opt.fullName();
    }

}
