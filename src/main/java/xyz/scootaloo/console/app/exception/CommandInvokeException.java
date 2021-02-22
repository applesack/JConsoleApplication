package xyz.scootaloo.console.app.exception;

/**
 * 命令执行时异常
 * @author flutterdash@qq.com
 * @since 2021/2/21 23:22
 */
public class CommandInvokeException extends ConsoleAppRuntimeException {

    public CommandInvokeException(String msg) {
        super(msg);
    }

    public CommandInvokeException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
