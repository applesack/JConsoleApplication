package xyz.scootaloo.console.app.error;

import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.MethodMeta;
import xyz.scootaloo.console.app.parser.ParameterParser;
import xyz.scootaloo.console.app.util.ClassUtils;

import java.io.PrintStream;
import java.util.List;

/**
 * 控制台应用运行期间产生的异常
 * @see CommandInvokeException 命令执行时的异常
 * @see ParameterResolveException 参数解析时的异常
 *
 * @author flutterdash@qq.com
 * @since 2021/2/21 23:12
 */
public abstract class ConsoleAppRuntimeException extends RuntimeException {

    private final long timestamp;   // 发生异常时的时间戳
    private Object obj;             // 此方法所属的对象
    private String methodDescribe;  // 被执行的方法的描述
    private String input;           // 命令行的输入(参数部分)
    private Class<? extends ParameterParser> parser; // 解析此命令行使用的解析器
    private ErrorCode errorCode = ErrorCode.DEFAULT_ERROR;

    public ConsoleAppRuntimeException() {
        this("");
    }

    public ConsoleAppRuntimeException(String msg) {
        super(msg);
        this.timestamp = System.currentTimeMillis();
    }

    public ConsoleAppRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }

    public ConsoleAppRuntimeException appendExData(MethodMeta meta,
                                                   Object obj,
                                                   List<String> input,
                                                   Class<? extends ParameterParser> parserClass) {
        this.methodDescribe = ClassUtils.getMethodDescribe(meta.method);
        if (input != null)
            this.input = String.join(" ", input);
        this.parser = parserClass;
        return this;
    }

    public ConsoleAppRuntimeException setErrorInfo(ErrorCode code) {
        this.errorCode = code;
        return this;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        ResourceManager.getConsole().err(errorCode);
        super.printStackTrace(s);
    }

    // getter

    public long getTimestamp() {
        return timestamp;
    }

    public Object getObj() {
        return obj;
    }

    public String getMethodDescribe() {
        return methodDescribe;
    }

    public String getInput() {
        return input;
    }

    public Class<? extends ParameterParser> getParser() {
        return parser;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
