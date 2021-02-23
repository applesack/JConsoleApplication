package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;

/**
 * 结果包装
 * @see ParameterWrapper 一般情况下使用这个
 * @author flutterdash@qq.com
 * @since 2021/1/16 22:59
 */
public interface ResultWrapper {

    /**
     * @return 是否解析成功，假如解析不成功，{@code getEx} 方法将返回解析时遇到的异常
     */
    boolean isSuccess();

    /**
     * @return 如果解析成功，这个方法将返回供java方法调用的参数
     */
    Object[] getArgs();

    /**
     * @return 解析过程中遇到的异常
     */
    ConsoleAppRuntimeException getEx();

}
