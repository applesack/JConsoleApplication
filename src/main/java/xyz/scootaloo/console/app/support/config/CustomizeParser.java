package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.DefaultValueConfigBuilder;
import xyz.scootaloo.console.app.support.parser.ParameterParser;
import xyz.scootaloo.console.app.support.parser.NameableParameterParser;

import java.util.HashMap;
import java.util.Map;

/**
 * 注册自定义的参数解析器
 * @author flutterdash@qq.com
 * @since 2021/1/18 11:53
 */
public class CustomizeParser {

    private final DefaultValueConfigBuilder builder;
    protected final Map<String, ParameterParser> parserMap;

    public CustomizeParser(DefaultValueConfigBuilder builder) {
        this.builder = builder;
        parserMap = new HashMap<>();
    }

    public CustomizeParser addParser(String name, ParameterParser converter) {
        if (name != null && converter != null) {
            parserMap.put(name, converter);
        }
        return this;
    }

    public CustomizeParser addParser(NameableParameterParser converter) {
        if (converter != null) {
            this.parserMap.put(converter.name(), converter);
        }
        return this;
    }

    public DefaultValueConfigBuilder ok() {
        builder.setParserMap(this);
        return builder;
    }

}
