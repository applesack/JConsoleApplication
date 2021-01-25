package xyz.scootaloo.console.app.common;

/**
 * 一些通用的便捷方法，实现此接口可以快捷的调用
 * @author flutterdash@qq.com
 * @since 2020/12/28 15:17
 */
public abstract class DefaultConsole implements Console {

    @Override
    public void print(Object z) {
        System.out.print(z);
    }

    @Override
    public void println(Object z) {
        System.out.println(z);
    }

}
