package xyz.scootaloo.console.app.support.parser;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 不做任何操作的转换器
 * 只是将参数还原成原始的格式，交给方法调用
 * @author flutterdash@qq.com
 * @since 2021/1/16 23:03
 */
public class SimpleConverter implements Converter {
    // 单例
    protected static final SimpleConverter INSTANCE = new SimpleConverter();

    private SimpleConverter() {
    }

    @Override
    public Wrapper convert(Method method, List<String> arg) {
        return new SimpleWrapper(arg);
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
