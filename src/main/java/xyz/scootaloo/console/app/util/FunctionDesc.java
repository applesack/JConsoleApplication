package xyz.scootaloo.console.app.util;

/**
 * 各种函数的接口描述
 * <pre>
 * 从无参到四参
 * 有返回值和无返回值
 * 全覆盖</pre>
 * @see InvokeProxy
 * @author flutterdash@qq.com
 * @since 2021/2/13 17:07
 */
public final class FunctionDesc {

    /** 描述有返回值，没有参数的方法 */
    @FunctionalInterface
    public interface Rtn0P<R> {
        R call() throws Exception;
    }

    /** 描述有返回值，有一个参数的方法 */
    @FunctionalInterface
    public interface Rtn1P<R, T> {
        R call(T param) throws Exception;
    }

    /** 描述有返回值，有两个参数的方法 */
    @FunctionalInterface
    public interface Rtn2P<R, T1, T2> {
        R call(T1 param1, T2 param2) throws Exception;
    }

    /** 描述有返回值，有三个参数的方法 */
    @FunctionalInterface
    public interface Rtn3P<R, T1, T2, T3> {
        R call(T1 param1, T2 param2, T3 param3) throws Exception;
    }

    /** 描述有返回值，有四个参数的方法 */
    @FunctionalInterface
    public interface Rtn4P<R, T1, T2, T3, T4> {
        R call(T1 param1, T2 param2, T3 param3, T4 param4) throws Exception;
    }

    /** 描述没有有返回值，没有参数的方法 */
    @FunctionalInterface
    public interface NonRtn0P {
        void call() throws Exception;
    }

    /** 描述没有返回值，有一个参数的方法 */
    @FunctionalInterface
    public interface NonRtn1P<T> {
        void call(T param) throws Exception;
    }

    /** 描述没有返回值，有两个参数的方法 */
    @FunctionalInterface
    public interface NonRtn2P<T1, T2> {
        void call(T1 param1, T2 Param2) throws Exception;
    }

    /** 描述没有返回值，有三个参数的方法 */
    @FunctionalInterface
    public interface NonRtn3P<T1, T2, T3> {
        void call(T1 param1, T2 Param2, T3 param3) throws Exception;
    }

    /** 描述没有返回值，有四个参数的方法 */
    @FunctionalInterface
    public interface NonRtn4P<T1, T2, T3, T4> {
        void call(T1 param1, T2 Param2, T3 param3, T4 param4) throws Exception;
    }

    /**
     * 获取方法代理对象的实例
     * @return 单例
     */
    public static InvokeProxy getInvokeProxyInstance() {
        return InvokeProxy.getInstance();
    }

}
