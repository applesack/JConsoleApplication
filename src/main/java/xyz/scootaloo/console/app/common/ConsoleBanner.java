package xyz.scootaloo.console.app.common;


/**
 * 应用启动时 printBanner() 方法被调用，可以在这个方法内打印欢迎信息
 * @author flutterdash@qq.com
 * @since 2021/2/27 11:54
 */
@FunctionalInterface
public interface ConsoleBanner extends Factory {

    void printBanner();

}
