package xyz.scootaloo.console.app.support.parser;

import lombok.Getter;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/7 9:32
 */
@Getter
public class InvokeInfo {

    private boolean success;
    private Object rtnVal;
    private Class<?> rtnType;
    private List<String> cmdItems;
    private Object[] methodArgs;
    private Exception exception;
    private String exMsg;
    private long interval;
    private long invokeAt;

    private InvokeInfo() {
    }

    // 因为某种原因，方法没有被执行
    public static InvokeInfo failed(Class<?> rtnType, List<String> cmdItems, Exception ex) {
        InvokeInfo info = new InvokeInfo();
        info.success = false;
        info.exception = ex;
        info.exMsg = ex.getMessage();
        info.invokeAt = 0;
        info.interval = 0;
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
