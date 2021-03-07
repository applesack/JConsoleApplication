package xyz.scootaloo.console.app.application.processor;

import xyz.scootaloo.console.app.parser.InvokeInfo;

/**
 * 提供一个处理器来接受命令行解析的返回值
 *
 * @author flutterdash@qq.com
 * @since 2021/2/16 14:59
 */
@FunctionalInterface
public interface PostProcessor {

    /**
     * 控制台的命令被执行后，会得到一个 {@code InvokeInfo} 对象
     * @param info 命令的执行信息
     */
    void process(InvokeInfo info);

}
