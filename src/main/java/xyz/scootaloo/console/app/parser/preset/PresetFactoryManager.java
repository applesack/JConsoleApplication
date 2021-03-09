package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.config.ConsoleConfig;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 管理预设的工厂<br>
 * 管理此包下的预设的接口实现
 * @author flutterdash@qq.com
 * @since 2021/1/25 17:39
 */
public final class PresetFactoryManager {

    /**
     * 返回所有工厂的全集
     * @param config 控制台配置
     * @return 具有先后顺序的工厂集合，先预设工厂，后用户工厂
     */
    public static Set<Supplier<Object>> getFactories(ConsoleConfig config) {
        Set<Supplier<Object>> factories = new LinkedHashSet<>();

        factories.add(() -> SimpleParameterParser.INSTANCE);     // 1
        factories.add(() -> SystemPresetCmd.INSTANCE);           // 2
        factories.add(() -> SystemPresetCmd.Help.INSTANCE);      // 3
        factories.add(() -> BackstageTask.INSTANCE);             // 4
        factories.add(() -> SubParameterParser.INSTANCE);        // 5
        factories.add(() -> VariableSetter.INSTANCE);            // 6
        factories.add(() -> CollectionParameterParser.INSTANCE); // 7

        factories.addAll(config.getFactories());
        return factories;
    }

}
