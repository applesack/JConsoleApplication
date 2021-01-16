package xyz.scootaloo.console.app.support.parser;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/16 22:59
 */
@FunctionalInterface
public interface Converter {

    Wrapper convert(List<String> arg);

}
