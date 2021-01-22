package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.List;

/**
 * 执行器
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
@FunctionalInterface
public interface Actuator {

    /**
     * 接收字符串列表做为参数，进行执行
     * @param items 字符串参数
     * @return 执行结果
     */
    InvokeInfo invoke(List<String> items);

}
