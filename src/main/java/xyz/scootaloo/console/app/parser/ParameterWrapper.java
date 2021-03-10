package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;

import java.util.Arrays;
import java.util.List;

/**
 * 参数包装器
 * <p>提供一些静态方法，</p>
 * @author flutterdash@qq.com
 * @since 2021/1/25 20:01
 */
public final class ParameterWrapper implements ResultWrapper {
    /** meta */
    private final boolean success;
    private final Object[] args;
    private final ConsoleAppRuntimeException ex;

    public static ParameterWrapper success(List<Object> argList) {
        return new ParameterWrapper(true, argList, null);
    }

    public static ParameterWrapper fail(ConsoleAppRuntimeException e) {
        return new ParameterWrapper(false, null, e);
    }

    private ParameterWrapper(boolean success, List<Object> argList, ConsoleAppRuntimeException ex) {
        this.ex = ex;
        this.success = success;
        if (argList != null)
            this.args = argList.toArray();
        else
            this.args = null;
    }

    // getter

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public ConsoleAppRuntimeException getEx() {
        return ex;
    }

    @Override
    public String toString() {
        return "ParameterWrapper{" +
                "success=" + success +
                ", args=" + Arrays.toString(args) +
                ", ex=" + ex +
                '}';
    }

}
