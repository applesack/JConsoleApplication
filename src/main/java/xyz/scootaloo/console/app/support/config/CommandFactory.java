package xyz.scootaloo.console.app.support.config;

import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import static xyz.scootaloo.console.app.support.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

/**
 * 增加命令工厂时使用的构建者类
 * @author flutterdash@qq.com
 * @since 2021/1/6 22:17
 */
public class CommandFactory {

    protected final Set<Supplier<Object>> commandFac;
    private final DefaultValueConfigBuilder builder;

    public CommandFactory(DefaultValueConfigBuilder builder) {
        this.commandFac = new LinkedHashSet<>();
        this.builder = builder;
    }

    public CommandFactory add(Class<?> factory) {
        this.commandFac.add(() -> {
            try {
                return ClassUtils.newInstance(factory);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
                System.exit(0);
                return null;
            }
        });
        return this;
    }

    public CommandFactory add(Supplier<Object> factory, boolean enable) {
        if (enable)
            this.commandFac.add(factory);
        return this;
    }

    public CommandFactory add(Object factory, boolean enable) {
        if (enable)
            this.commandFac.add(() -> factory);
        return this;
    }

    public CommandFactory add(Class<?> factory, boolean enable) {
        if (enable)
            this.add(factory);
        return this;
    }

    public DefaultValueConfigBuilder ok() {
        this.builder.setCommandFactories(this);
        return builder;
    }

}
