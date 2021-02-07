package xyz.scootaloo.console.app.config;

import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.PackScanner;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

/**
 * 增加命令工厂时使用的构建者类
 * @author flutterdash@qq.com
 * @since 2021/1/6 22:17
 */
public final class FactoryCollector {

    protected final Set<Supplier<Object>> commandFac;
    private final DefaultValueConfigBuilder builder;

    public FactoryCollector(DefaultValueConfigBuilder builder) {
        this.commandFac = new LinkedHashSet<>();
        this.builder = builder;
    }

    public FactoryCollector add(Class<?> factory) {
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

    public FactoryCollector scanPack() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        return scanPack(StringUtils.getPack(caller.getClassName()));
    }

    public FactoryCollector scanPack(String packName) {
        commandFac.addAll(PackScanner.getClasses(packName)
                .stream()
                .map(ClassUtils::facSupplier)
                .collect(Collectors.toList()));
        return this;
    }

    public FactoryCollector add(Supplier<Object> factory, boolean enable) {
        if (enable)
            this.commandFac.add(factory);
        return this;
    }

    public FactoryCollector add(Object factory, boolean enable) {
        if (enable)
            this.commandFac.add(() -> factory);
        return this;
    }

    public FactoryCollector add(Class<?> factory, boolean enable) {
        if (enable)
            this.add(factory);
        return this;
    }

    // 返回默认值构建者继续配置
    public DefaultValueConfigBuilder then() {
        this.builder.setCommandFactories(this);
        return builder;
    }

    // 直接得到配置
    public ConsoleConfig ok() {
        then();
        return builder.build();
    }

}
