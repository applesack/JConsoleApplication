package xyz.scootaloo.console.app.support.config;

import java.util.ArrayList;
import java.util.List;

import static xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.*;

/**
 * @author flutterdash@qq.com
 * @since 2021/1/6 22:17
 */
public class CommandFactory {

    protected final List<Class<?>> commandFac;
    private final DefaultValueConfigBuilder builder;

    public CommandFactory(DefaultValueConfigBuilder builder) {
        this.commandFac = new ArrayList<>();
        this.builder = builder;
    }

    public CommandFactory add(Class<?> factory) {
        this.commandFac.add(factory);
        return this;
    }

    public CommandFactory add(Class<?> factory, boolean enable) {
        if (enable)
            this.commandFac.add(factory);
        return this;
    }

    public DefaultValueConfigBuilder ok() {
        this.builder.setCommandFactories(this);
        return builder;
    }

}
