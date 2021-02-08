package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.parser.MethodMeta;
import xyz.scootaloo.console.app.parser.NameableParameterParser;
import xyz.scootaloo.console.app.parser.ParameterWrapper;
import xyz.scootaloo.console.app.parser.Wrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 不做任何操作的转换器
 * 只是将参数还原成原始的格式，交给方法调用
 * @author flutterdash@qq.com
 * @since 2021/1/16 23:03
 */
public final class SimpleParameterParser implements NameableParameterParser {
    // 单例
    protected static final SimpleParameterParser INSTANCE = new SimpleParameterParser();

    private static final String NAME = "raw";

    private SimpleParameterParser() {
    }

    @Override
    public Wrapper parse(MethodMeta meta, List<String> args) {
        ArrayList<Object> list = new ArrayList<>();
        list.add(String.join(" ", args));
        return ParameterWrapper.success(list);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean check(MethodMeta meta) {
        if (meta.size != 1)
            return false;
        return meta.parameterTypes[0] == String.class;
    }

}
