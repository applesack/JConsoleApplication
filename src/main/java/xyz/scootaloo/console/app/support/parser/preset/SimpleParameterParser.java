package xyz.scootaloo.console.app.support.parser.preset;

import xyz.scootaloo.console.app.support.parser.NameableParameterParser;
import xyz.scootaloo.console.app.support.parser.Wrapper;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 不做任何操作的转换器
 * 只是将参数还原成原始的格式，交给方法调用
 * @author flutterdash@qq.com
 * @since 2021/1/16 23:03
 */
public class SimpleParameterParser implements NameableParameterParser {
    // 单例
    public static final SimpleParameterParser INSTANCE = new SimpleParameterParser();

    private SimpleParameterParser() {
    }

    @Override
    public Wrapper parse(Method method, List<String> arg) {
        return new SimpleWrapper(arg);
    }

    @Override
    public String name() {
        return "raw";
    }

    @Override
    public boolean check(Method method) {
        if (method.getParameterCount() != 1)
            return false;
        return method.getParameterTypes()[0] == String.class;
    }

    protected static class SimpleWrapper implements Wrapper {

        private final Object[] args;

        public SimpleWrapper(List<String> args) {
            this.args = new Object[]{String.join(" ", args)};
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public Object[] getArgs() {
            return args;
        }

        @Override
        public Exception getEx() {
            return null;
        }

    }

}
