package xyz.scootaloo.console.app.parser;

/**
 * 给参数解析器起个名字
 * @author flutterdash@qq.com
 * @since 2021/1/18 11:44
 */
public interface NameableParameterParser extends ParameterParser {

    // 解析器的名称，这样可用按照字符串来找到对应的解析器
    String name();

}
