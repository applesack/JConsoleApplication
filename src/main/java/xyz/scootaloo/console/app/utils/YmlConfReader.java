package xyz.scootaloo.console.app.utils;

import org.yaml.snakeyaml.Yaml;
import xyz.scootaloo.console.app.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.config.ConsoleConfigProvider.DefaultValueConfigBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Function;

/**
 * 读取yml配置
 * 参考 classpath:/console.yml
 * @author flutterdash@qq.com
 * @since 2021/1/19 13:57
 */
public class YmlConfReader {

    public static final String DFT_FILENAME = "console.yml";
    private static final Console console = ResourceManager.getConsole();
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
        File configFile = new File(Objects.requireNonNull(ResourceManager
                .getLoader().getResource(filename)).getFile());
        if (!configFile.exists())
            return;
        try {
            loadConf(builder, new Yaml().load(new FileInputStream(configFile.getAbsolutePath())));
        } catch (FileNotFoundException ignored) {
            // ignore
        }
    }

    private static void loadConf(DefaultValueConfigBuilder configBuilder, Map<String, Object> map) {
        Map<String, Object> consoleConf = Console.dbEx(YmlConfReader::getFrom, map, "console");
        if (consoleConf != null) {
            ClassUtils.loadPropFromMap(configBuilder, consoleConf, null, converterMap);
        }
        Map<String, Object> authorConf = Console.dbEx(YmlConfReader::getFrom, map, "author");
        if (authorConf != null) {
            ClassUtils.loadPropFromMap(configBuilder, authorConf, "author", converterMap);
        }
        List<Object> inits = Console.dbEx(YmlConfReader::getFrom, map, "init");
        if (inits != null) {
            doGetInitCommands(configBuilder, inits);
        }
        // 暂时不支持这个功能, 不做处理
        List<Object> factories = Console.dbEx(YmlConfReader::getFrom, map, "factories");
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
