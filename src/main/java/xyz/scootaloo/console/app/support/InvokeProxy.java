package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.support.FunctionDesc.*;

/**
 * 传递或者调用会抛出异常的方法
 *
 * @author flutterdash@qq.com
 * @since 2021/2/12 0:21
 */
public final class InvokeProxy {

    public static <R> Rtn0pWrapper<R> fun(Rtn0P<R> rtn0P) {
        return new Rtn0pWrapper<>(rtn0P);
    }

    public static <R, T> Rtn1pWrapper<R, T> fun(Rtn1P<R, T> rtn1P) {
        return new Rtn1pWrapper<>(rtn1P);
    }

    public static <R, T1, T2> Rtn2pWrapper<R, T1, T2> fun(Rtn2P<R, T1, T2> rtn2P) {
        return new Rtn2pWrapper<>(rtn2P);
    }

    public static <R, T1, T2, T3> Rtn3pWrapper<R, T1, T2, T3> fun(Rtn3P<R, T1, T2, T3> rtn3P) {
        return new Rtn3pWrapper<>(rtn3P);
    }

    public static <R, T1, T2, T3, T4> Rtn4pWrapper<R, T1, T2, T3, T4> fun(Rtn4P<R, T1, T2, T3, T4> rtn4P) {
        return new Rtn4pWrapper<>(rtn4P);
    }

    public static NonRtn0pWrapper fun(NonRtn0P nonRtn0P) {
        return new NonRtn0pWrapper(nonRtn0P);
    }

    public static <T> NonRtn1pWrapper<T> fun(NonRtn1P<T> nonRtn1P) {
        return new NonRtn1pWrapper<>(nonRtn1P);
    }

    public static <T1, T2> NonRtn2pWrapper<T1, T2> fun(NonRtn2P<T1, T2> nonRtn2P) {
        return new NonRtn2pWrapper<>(nonRtn2P);
    }

    public static <T1, T2, T3> NonRtn3pWrapper<T1, T2, T3> fun(NonRtn3P<T1, T2, T3> nonRtn3P) {
        return new NonRtn3pWrapper<>(nonRtn3P);
    }

    public static <T1, T2, T3, T4> NonRtn4pWrapper<T1, T2, T3, T4> fun(NonRtn4P<T1, T2, T3, T4> nonRtn4P) {
        return new NonRtn4pWrapper<>(nonRtn4P);
    }

}
