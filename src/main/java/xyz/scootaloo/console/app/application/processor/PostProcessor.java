package xyz.scootaloo.console.app.application.processor;

import xyz.scootaloo.console.app.parser.InvokeInfo;

/**
 * 提供一个处理器来处理命令的返回值，只用控制台模式有效. (启动方式分为解释器模式 和 控制台模式)
 * @see xyz.scootaloo.console.app.application.AbstractConsoleApplication#addPostProcessor(PostProcessor)
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
