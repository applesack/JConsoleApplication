package xyz.scootaloo.console.app.support.application;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
public interface Invoker {

    String getName();

    void invoke(List<String> items);



}
