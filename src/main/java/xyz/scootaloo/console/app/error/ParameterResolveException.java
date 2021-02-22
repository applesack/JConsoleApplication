package xyz.scootaloo.console.app.error;

/**
 * 解析命令行参数时产生的异常
 * @author flutterdash@qq.com
 * @since 2021/2/21 23:10
 */
public class ParameterResolveException extends ConsoleAppRuntimeException {

    public ParameterResolveException(String msg) {
        super(msg);
    }

    public ParameterResolveException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
