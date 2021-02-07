package xyz.scootaloo.console.app.common;

/**
 * 输出接口
 * 将此接口放在方法参数上，运行命令方法时，系统将自动注入
 * @author flutterdash@qq.com
 * @since 2021/2/5 20:46
 */
public interface OutPrinter {

    void print(Object o);

    void println(Object o);

    void err(Object o);

}
