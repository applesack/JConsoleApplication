package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;

import java.util.Arrays;

/**
 * 包含 java方法 执行结果的信息, 由 {@link Interpreter} 产生
 *
 * @author flutterdash@qq.com
 * @since 2021/1/7 9:32
 */
public final class InvokeInfo {

    private String name;             // 被执行的方法名，或者命令名
    private Object rtnVal;           // 方法返回值
    private Class<?> rtnType;        // 返回值类型
    private String cmdArgs;          // 执行此方法所使用的字符串命令
    private Object[] methodRealArgs; // 经过解析后得到的方法参数数组
    private long interval;           // 方法执行所用的时间
    private long invokeAt;           // 从何时开始执行此方法

    private boolean success;  // 是否执行成功
    private String exMsg;     // 异常信息
    private ConsoleAppRuntimeException exception;   // 执行方法时遇到的异常

    private InvokeInfo() {
    }

    // 因为某种原因，方法没有被执行
    public static InvokeInfo failed(Class<?> rtnType, String cmdArgs, ConsoleAppRuntimeException ex) {
        InvokeInfo info = new InvokeInfo();
        info.success = false;
        info.exception = ex;
        info.rtnType = rtnType;
        info.cmdArgs = cmdArgs;
        info.exMsg = ex.getMessage();
        info.invokeAt = 0;
        info.interval = 0;
        return info;
    }

    // 缺省的成功调用
    public static InvokeInfo simpleSuccess() {
        InvokeInfo info = new InvokeInfo();
        info.success = true;
        return info;
    }

    // 填充属性
    public static InvokeInfo beforeInvoke(String name, Class<?> rtnType, String cmdItems) {
        InvokeInfo info = new InvokeInfo();
        info.name = name;
        info.rtnType = rtnType;
        info.cmdArgs = cmdItems;
        info.invokeAt = System.currentTimeMillis();
        return info;
    }

    // 异常时
    protected InvokeInfo onException(ConsoleAppRuntimeException e, Object[] methodArgs) {
        this.success = false;
        this.methodRealArgs = methodArgs;
        this.rtnVal = null;
        this.exception = e;
        this.exMsg = e.getMessage();
        this.interval = System.currentTimeMillis() - this.invokeAt;
        return this;
    }

    // 对应 beforeInvoke ，方法执行完成后
    protected void finishInvoke(Object rtnVal, Object[] methodArgs) {
        this.success = true;
        this.rtnVal = rtnVal;
        this.methodRealArgs = methodArgs;
        this.exception = null;
        this.exMsg = null;
        this.interval = System.currentTimeMillis() - this.invokeAt;
    }

    /**
     * 获取返回值
     * <pre>建议在获取返回值之前检查 success 属性是否为 true，对方法执行失败的情况做一定处理。
     * 否则方法执行失败时 get() 方法返回null，会与正常结果混淆。</pre>
     * @param <T> 免除手动类型转换
     * @return 命令方法的返回值
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
        return (T) rtnVal;
    }

    // getter

    public String getName() {
        return this.name;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public Object getRtnVal() {
        return this.rtnVal;
    }

    public Class<?> getRtnType() {
        return this.rtnType;
    }

    public String getCmdArgs() {
        return this.cmdArgs;
    }

    public Object[] getMethodRealArgs() {
        return this.methodRealArgs;
    }

    public ConsoleAppRuntimeException getException() {
        return this.exception;
    }

    public String getExMsg() {
        return this.exMsg;
    }

    public long getInterval() {
        return this.interval;
    }

    public long getInvokeAt() {
        return this.invokeAt;
    }

    @Override
    public String toString() {
        return "InvokeInfo{" +
                "name='" + name + '\'' +
                ", rtnVal=" + rtnVal +
                ", rtnType=" + rtnType +
                ", cmdArgs=" + cmdArgs +
                ", methodArgs=" + Arrays.toString(methodRealArgs) +
                ", interval=" + interval +
                ", invokeAt=" + invokeAt +
                ", success=" + success +
                ", exMsg='" + exMsg + '\'' +
                ", exception=" + exception +
                '}';
    }

}
