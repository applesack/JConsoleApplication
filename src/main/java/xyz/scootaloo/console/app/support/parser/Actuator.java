package xyz.scootaloo.console.app.support.parser;

import java.util.List;

/**
 * 执行器
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
@FunctionalInterface
public interface Actuator {

    InvokeInfo invoke(List<String> items) throws Exception;

}
