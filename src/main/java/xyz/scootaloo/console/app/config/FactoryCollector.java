package xyz.scootaloo.console.app.config;

import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.PackScanner;
import xyz.scootaloo.console.app.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;
import static xyz.scootaloo.console.app.util.InvokeProxy.fun;

/**
 * 增加命令工厂时使用的构建者类
 *
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

    /**
     * 增加一个类对象，根据这个类对象获取它的实例，并装配到框架
     * @param factory 一个工厂类的类型
     * @return 构建者
     */
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

    /**
     * 扫描调用者所在的包
     * @return 构建者
     */
    public FactoryCollector scanPack() {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        return scanPack(StringUtils.getPack(caller.getClassName()));
    }

    /**
     * 扫描一个包下的所有类，并将这些类实例化
     * <p>目前的策略是遍历检查这个包下的所有的类(包括子包)，如果这个类中含有名为 {@code FACTORY_INSTANCE} 或者
     * {@code INSTANCE} 的静态常量，且这个静态常量的类型和当前类的类型一致，则获取这个变量值，装配到框架的容器中。<br> 例:</p>
     * <pre>{@code public class MyFactory {
     *     private static final MyFactory INSTANCE = new MyFactory();
     * }}</pre>
     * <p>这个名为"INSTANCE"的变量会被装配</p>
     * @param packName 包名
     * @return 构建者
     */
    public FactoryCollector scanPack(String packName) {
        String factory_name = "FACTORY_INSTANCE";
        String instance_name = "INSTANCE";
        commandFac.addAll(PackScanner.getClasses(packName).stream()
                .map(classType ->
                        Stream.of(
                                fun(classType::getDeclaredField).call(factory_name),
                                fun(classType::getDeclaredField).call(instance_name)
                        )
                                .filter(Objects::nonNull)
                                .filter(curType -> curType.getType() == classType)
                                .map(field -> {
                                    field.setAccessible(true);
                                    return fun(field::get).call(new Object[] { null });
                                })
                                .filter(Objects::nonNull)
                                .findAny().orElse(null)
                ).filter(Objects::nonNull)
                .map(obj -> (Supplier<Object>) () -> obj).collect(Collectors.toList()));
        return this;
    }

    /**
     * @param factory 一个工厂提供者，通过调用它的 {@code get()} 方法获取工厂的实例
     * @param enable 是否启用，只有这个为 true， 才会进行装配
     * @return 构建者
     */
    public FactoryCollector add(Supplier<Object> factory, boolean enable) {
        if (enable)
            this.commandFac.add(factory);
        return this;
    }

    /**
     * @param factory 工厂类的实例
     * @param enable 是否启用，只有这个为 true， 才会进行装配
     * @return 构建者
     */
    public FactoryCollector add(Object factory, boolean enable) {
        if (enable)
            this.commandFac.add(() -> factory);
        return this;
    }

    /**
     * @param factory {@link #add(Object, boolean)}
     * @param enable {@link #add(Object, boolean)}
     * @return 构建者
     */
    public FactoryCollector add(Class<?> factory, boolean enable) {
        if (enable)
            this.add(factory);
        return this;
    }

    /**
     * @return 返回默认值构建者继续配置
     */
    public DefaultValueConfigBuilder then() {
        this.builder.setCommandFactories(this);
        return builder;
    }

    /**
     * @return 直接得到配置
     */
    public ConsoleConfig ok() {
        then();
        return builder.build();
    }

}
