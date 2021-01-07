package xyz.scootaloo.console.app.support.parser;

import lombok.Getter;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/7 9:32
 */
@Getter
public class InvokeInfo {

    private boolean success;       // 是否执行成功
    private Object rtnVal;         // 方法返回值
    private Class<?> rtnType;      // 返回值类型
    private List<String> cmdItems; // 执行此方法所使用的字符串命令
    private Object[] methodArgs;   // 经过解析后得到的方法参数数组
    private Exception exception;   // 执行方法时遇到的异常
    private String exMsg;          // 异常信息
    private long interval;         // 方法执行所用的时间
    private long invokeAt;         // 从何时开始执行此方法

    private InvokeInfo() {
    }

    // 因为某种原因，方法没有被执行
    public static InvokeInfo failed(Class<?> rtnType, List<String> cmdItems, Exception ex) {
        InvokeInfo info = new InvokeInfo();
        info.success = false;
        info.exception = ex;
        info.rtnType = rtnType;
        info.cmdItems = cmdItems;
        info.exMsg = ex.getMessage();
        info.invokeAt = 0;
        info.interval = 0;
        return info;
    }

    public static InvokeInfo simpleSuccess() {
        InvokeInfo info = new InvokeInfo();
        info.success = true;
        return info;
    }

    // 填充属性
    public static InvokeInfo beforeInvoke(Class<?> rtnType, List<String> cmdItems) {
        InvokeInfo info = new InvokeInfo();
        info.rtnType = rtnType;
        info.cmdItems = cmdItems;
        info.invokeAt = System.currentTimeMillis();
        return info;
    }

    // 异常时
    protected void onException(Exception e, Object[] methodArgs) {
        this.success = false;
        this.methodArgs = methodArgs;
        this.rtnVal = null;
        this.exception = e;
        this.exMsg = e.getMessage();
        this.interval = System.currentTimeMillis() - this.invokeAt;
    }

    protected void finishInvoke(Object rtnVal, Object[] methodArgs) {
        this.success = true;
        this.rtnVal = rtnVal;
        this.methodArgs = methodArgs;
        this.exception = null;
        this.exMsg = null;
        this.interval = System.currentTimeMillis() - this.invokeAt;
    }

    public Object get() {
        return rtnVal;
    }

}
