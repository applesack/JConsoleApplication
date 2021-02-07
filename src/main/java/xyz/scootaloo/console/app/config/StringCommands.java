package xyz.scootaloo.console.app.config;

import xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 增加初始化命令的构建者类
 * @author flutterdash@qq.com
 * @since 2021/1/6 22:08
 */
public final class StringCommands {

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
