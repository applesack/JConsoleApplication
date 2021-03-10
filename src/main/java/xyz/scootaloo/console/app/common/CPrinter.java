package xyz.scootaloo.console.app.common;

import java.io.OutputStream;

/**
 * 输出接口 <br>
 * 将此接口放在方法参数上，运行命令方法时，系统将自动注入
 * @author flutterdash@qq.com
 * @since 2021/2/5 20:46
 */
public abstract class CPrinter extends OutputStream {

    /**
     * 输出内容，不换行
     * @param o 对象
     */
    public abstract void print(Object o);

    /**
     * 输出内容，末尾自动换行
     * @param o 对象
     */
    public abstract void println(Object o);

    /**
     * 输出醒目的错误信息
     * @param o 对象
     */
    public abstract void err(Object o);

    /**
     * 设置模式
     * true, 立刻输出信息，默认为true.
     * false, 所有print()或者println()或者err()的调用将会把信息缓存起来，延迟到refresh()调用才会把缓存的内容一次性输出。
     * @param immediate 是否立刻输出信息。
     */
    public void setMode(boolean immediate) {

    }

    /**
     * 刷新缓存的内容，同时将缓存的内容一次性输出
     */
    public void refresh() {

    }

    @Override
    public void write(int b) {
        print(b);
    }

}
