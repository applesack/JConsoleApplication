package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.config.ConsoleConfig;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 管理预设的工厂
 * @author flutterdash@qq.com
 * @since 2021/1/25 17:39
 */
public class PresetManager {

    public static Set<Supplier<Object>> getFactories(ConsoleConfig config) {
        Set<Supplier<Object>> factories = new LinkedHashSet<>();

        factories.add(() -> SimpleParameterParser.INSTANCE);
        factories.add(() -> SystemPresetCmd.INSTANCE);
        factories.add(() -> SystemPresetCmd.Help.INSTANCE);

        factories.addAll(config.getFactories());
        return factories;
    }

}
