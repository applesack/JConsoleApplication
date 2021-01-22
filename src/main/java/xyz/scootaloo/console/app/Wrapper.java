package xyz.scootaloo.console.app;

/**
 * 结果包装
 * @author flutterdash@qq.com
 * @since 2021/1/16 22:59
 */
public interface Wrapper {

    // 解析是否成功
    boolean isSuccess();

    // 解析成功，得到的参数
    Object[] getArgs();

    // 解析过程中遇到的异常
    Exception getEx();

}
