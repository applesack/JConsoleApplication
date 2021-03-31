package xyz.scootaloo.console.app.error;

/**
 * 框架专属的错误代码
 * <pre>
 * 为了让框架的使用更加友好，对可能遇到的各种异常信息都加以解释，并给出解决方案。
 * 这些信息将在异常被抛出的时候被输出，另外你可以在捕获异常时从异常对象上拿到 {@code ErrorCode};
 *
 * @see ConsoleAppRuntimeException 控制台应用运行时异常
 * @see ConsoleAppRuntimeException#getErrorCode() 获取
 * </pre>
 * @author flutterdash@qq.com
 * @since 2021/3/1 11:44
 */
public enum ErrorCode {

    /** 100 ~ 199 未定义的错误 */
    DEFAULT_ERROR(100, "默认错误", "此错误未使用"),
    LACK_COMMAND_HANDLER(101, "命令方法", "命令行处理器未找到, 没有找到与此命令名对应的java方法"),

    /** 200 ~ 299 解析命令行参数时错误 */
    NONSUPPORT_TYPE(201, "类型解析错误", "方法参数中含有框架不支持的类型，将此参数替换成其他类型，或者向框架中注册这种类型的解析实现"),
    LACK_REQUIRED_PARAMETERS(202, "@Opt注解错误", "@Opt注解中设置了必选项，而命令行中缺少此参数。请检查命令行输入，或移除参数的必选属性"),
    PARAMETER_PARSER_ERROR(203, "参数解析器错误", "参数解析器在分析参数时抛出了异常，请检查此方法解析参数的逻辑是否正确"),
    LACK_PARAMETER(204, "默认解析器", "在默认参数解析器中, 没有标记@Opt注解的方法参数，不能在命令行中缺失"),
    RESOLVE_ERROR(205, "解析错误", "将命令行参数映射到方法参数时, 处理某个类型时遇到了异常"),

    /** 300 ~ 399 执行方法时错误 */
    METHOD_INVOKE_ERROR(301, "方法执行错误", "请检查传进来的参数是否正确，如果参数正常，请继续排查方法内部的异常"),

    /** 400 ~ 499 过滤器错误 */
    FILTER_ON_EXCEPTION(401, "过滤器异常", "过滤器方法中抛出了异常, 请根据调用栈检查该过滤器方法"),
    FILTER_INTERCEPT(402, "过滤器", "此命令行的执行被过滤器拦截")

    ;

    private final short code;        // 此错误的代码
    private final String type;       // 错误类型
    private final String comment;    // 解决此错误的方式

    ErrorCode(int code, String type, String comment) {
        this.code = (short) code;
        this.type = type;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "\nErrorCode{" +
                "code=" + code +
                ", type='" + type + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

}
