package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/6 22:08
 */
public class StringCommands {

    protected final List<String> commandList;
    private final DefaultValueConfigBuilder builder;

    public StringCommands(DefaultValueConfigBuilder builder) {
        commandList = new ArrayList<>();
        this.builder = builder;
    }

    public StringCommands add(String cmd) {
        this.commandList.add(cmd);
        return this;
    }

    // todo 从文件中读取初始化命令
    public StringCommands getFromFile(String fileName) {
        return this;
    }

    public DefaultValueConfigBuilder ok() {
        this.builder.setInitCommands(this);
        return this.builder;
    }

}
