package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.exception.ConsoleAppRuntimeException;

import java.util.List;

/**
 * 参数包装器
 * @author flutterdash@qq.com
 * @since 2021/1/25 20:01
 */
public final class ParameterWrapper implements ResultWrapper {

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

}
