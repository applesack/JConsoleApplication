package xyz.scootaloo.console.app.support.parser;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
@FunctionalInterface
public interface Actuator {

    Object invoke(List<String> items) throws Exception;

}
