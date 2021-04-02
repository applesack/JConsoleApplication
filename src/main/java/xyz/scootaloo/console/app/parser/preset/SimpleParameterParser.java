package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.parser.*;

import java.util.*;
import java.util.function.Function;

/**
 * raw
 *
 * 不做任何操作的转换器<br>
 * 只是将参数还原成原始的格式，交给方法调用
 *
 * @author flutterdash@qq.com
 * @since 2021/1/16 23:03
 */
@Stateless
public final class SimpleParameterParser extends FillParamInOrder<Object> implements NameableParameterParser {
    /** singleton */
    protected static final SimpleParameterParser INSTANCE = new SimpleParameterParser();
    private static final List<String> PLACEHOLDER = new ArrayList<>();
    private static final String RAW_STRING = "raw";

    private final Map<Class<?>, Function<MethodMeta.Context<Object>, Object>> ACCEPT_TYPES = new HashMap<>();
    private SimpleParameterParser() {
        // 默认只支持 String
        ACCEPT_TYPES.put(String.class, (state) -> state.getParam(RAW_STRING));
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) {
        ArrayList<Object> list = new ArrayList<>();
        list.add(String.join(args));
        return ParameterWrapper.success(list);
    }

    @Override
    protected MethodMeta.Context<Object> createContext(MethodMeta meta, String args) {
        return MethodMeta.Context.getInstance(meta, args, this::putRawString);
    }

    @Override
    protected void doResolveIfAnnoExist(MethodMeta.Context<Object> state, MethodMeta.CurrentParamInfo current) {
        fillParam(state, current);
    }

    @Override
    protected void doResolveIfAnnoMissing(MethodMeta.Context<Object> state, MethodMeta.CurrentParamInfo current) {
        fillParam(state, current);
    }

    private List<String> putRawString(MethodMeta.Context<Object> state, String args) {
        state.getKVPairs().put(RAW_STRING, args);
        return PLACEHOLDER;
    }

    // 只处理能接受的类型
    private void fillParam(MethodMeta.Context<Object> state, MethodMeta.CurrentParamInfo current) {
        if (ACCEPT_TYPES.containsKey(current.getParamType())) {
             state.addMethodArgument(ACCEPT_TYPES.get(current.getParamType()).apply(state));
        } else {
            state.addMethodArgument(TransformFactory.getDefVal(current.getParamType()));
        }
    }

    @Override
    public String name() {
        return RAW_STRING;
    }

    @Override
    public boolean check(MethodMeta meta) {
        if (meta.size != 1)
            return false;
        return meta.parameterTypes[0] == String.class;
    }

    @Override
    public String toString() {
        return getParserString();
    }

}
