package xyz.scootaloo.console.app.application.processor;

import xyz.scootaloo.console.app.parser.InvokeInfo;

/**
 * 提供一个处理器来处理命令的返回值
 * @author flutterdash@qq.com
 * @since 2021/2/16 14:59
 */
@FunctionalInterface
public interface PostProcessor {

    void process(InvokeInfo info);

}
