package xyz.scootaloo.console.app.parser;

import java.util.List;

/**
 * 命令行参数执行器
 * 每个命令行解析器都需要实现这个接口，抽象方法{@link #invoke(List)}表示这个命令接收到参数将如何处理。
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
@FunctionalInterface
public interface Actuator {

    /**
     * 接收字符串列表做为参数，进行执行
     * @param cmdArgs 命令行参数，已经使用空格分隔成了列表
     * @return 命令执行过程中收集到的信息，具体请查看 {@link InvokeInfo}
     */
    InvokeInfo invoke(List<String> cmdArgs);

}
