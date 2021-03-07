package xyz.scootaloo.console.app.util;

import org.yaml.snakeyaml.Yaml;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

import java.util.*;
import java.util.function.Function;

/**
 * 读取yml配置
 * 配置内容参考 classpath:/console.yml
 *
 * @author flutterdash@qq.com
 * @since 2021/1/19 13:57
 */
public final class YmlConfReader {
    // 默认的配置文件名
    public static final String DFT_FILENAME = "console.yml";
    private static final Map<String, Function<Object, Object>> converterMap = new HashMap<>();

    static {
        converterMap.put("exitCmd", (type) -> {
            if (type instanceof String) {
                String line = (String) type;
                return line.split(",");
            } else {
                throw new RuntimeException("不能处理的类型" + type.getClass().getSimpleName());
            }
        });
    }

    public static void loadConf(DefaultValueConfigBuilder builder) {
        String filename = builder.getConfigFileName();
        if (filename == null)
            return;
        try {
            loadConf(builder, new Yaml().load(ResourceManager.getLoader()
                    .getResourceAsStream(builder.getConfigFileName())));
        } catch (Exception ignore) {
            // ignore
        }
    }

    private static void loadConf(DefaultValueConfigBuilder configBuilder, Map<String, Object> map) {
        Optional<Map<String, Object>> consoleConf = Optional.ofNullable(YmlConfReader.getFrom(map, "console"));
        consoleConf.ifPresent(stringObjectMap ->
                ClassUtils.loadPropFromMap(configBuilder, stringObjectMap, null, converterMap));
        Optional<Map<String, Object>> devConf = Optional.ofNullable(YmlConfReader.getFrom(map, "dev"));
        devConf.ifPresent(stringObjectMap ->
                ClassUtils.loadPropFromMap(configBuilder, stringObjectMap, null, converterMap));
        Optional<Map<String, Object>> authorConf = Optional.ofNullable(YmlConfReader.getFrom(map, "author"));
        authorConf.ifPresent(stringObjectMap ->
                ClassUtils.loadPropFromMap(configBuilder, stringObjectMap, "author", converterMap));
        Optional<List<Object>> inits = Optional.ofNullable(YmlConfReader.getFrom(map, "init"));
        inits.ifPresent(objects ->
                doGetInitCommands(configBuilder, objects));
        // 暂时不支持这个功能, 不做处理
//        List<Object> factories = Console.dbEx(YmlConfReader::getFrom, map, "factories");
    }

    private static void doGetInitCommands(DefaultValueConfigBuilder builder, List<Object> cmd) {
        List<String> res = new ArrayList<>();
        for (Object obj : cmd) {
            if (obj instanceof String) {
                res.add((String) obj);
            }
        }
        if (!res.isEmpty())
            ClassUtils.set(builder, "initCommands", res);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getFrom(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null)
            return null;
        return (T) obj;
    }

}
