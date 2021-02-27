package xyz.scootaloo.console.app.common;

/**
 * 输出欢迎界面
 * @author flutterdash@qq.com
 * @since 2021/2/27 10:48
 */
@FunctionalInterface
public interface ConsoleBanner extends Factory {

    void printBanner();

}
