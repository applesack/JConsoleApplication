package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.mark.NoStatus;
import xyz.scootaloo.console.app.client.ReplacementRecord;
import xyz.scootaloo.console.app.common.CPrinter;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.util.ClassUtils;
import xyz.scootaloo.console.app.util.VariableManager;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 转换工厂<br>
 * 实现字符串到指定类型的转换
 * @author flutterdash@qq.com
 * @since 2020/12/29 21:47
 */
@NoStatus("如果保证在执行命令之前完成所有的装配，就不会有并发问题")
public final class TransformFactory {
    /** resource */
    private static final Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<>(16);
    private static final Map<Class<?>, Function<String , Object>> STR_RESOLVE_MAP = new HashMap<>(16);
    private static final Map<Class<?>, Supplier<Object>> PRESET_VALUES_MAP = new HashMap<>();

    private static final Random random = ResourceManager.getRandom();

    static {
        // 设置各种基本类型的默认值 转换方式
        putDefVal(str -> str);
        putDefVal(int.class    , Integer.class, 0, Integer::parseInt  );
        putDefVal(short.class  , Short.class  , 0, Short::parseShort  );
        putDefVal(float.class  , Float.class  , 0, Float::parseFloat  );
        putDefVal(double.class , Double.class , 0, Double::parseDouble);
        putDefVal(long.class   , Long.class   , 0, Long::parseLong    );
        putDefVal(byte.class   , Byte.class   , 0, Byte::parseByte    );
        putDefVal(boolean.class, Boolean.class, false,
                str -> {
                    if (str == null || str.isEmpty())
                        return false;
                    str = str.toLowerCase(Locale.ROOT);
                    return str.startsWith("t");
                }
        );

        // 设置系统预设的一些实例
        putPresetObj(Random.class, () -> random);
        putPresetObj(CPrinter.class, ResourceManager::getPrinter);
    }

    /**
     * 获取此类型的默认值
     * @param type 类型
     * @return 类型的默认值，假如容器中不存在此默认值，则返回null; (容器中默认只包含基本类型的默认值)
     */
    public static Object getDefVal(Class<?> type) {
        return DEFAULT_VALUE_MAP.getOrDefault(type, null);
    }

    /**
     * 获取系统预设的值
     * <p>通常是一些单例，或者工厂方法管理的类型，默认框架提供一个 {@code Random} 对象，和一个 {@code CPrinter} 对象</p>
     * @param type 类型
     * @return 此类型的实例，假如不存在此实例则返回 empty
     */
    public static Optional<Object> getPresetVal(Class<?> type) {
        return Optional.ofNullable(PRESET_VALUES_MAP.get(type))
                .map(Supplier::get);
    }

    /**
     * 向工厂中放置预设的值
     * @param type 指定一个类型
     * @param supplier 此类型的工厂方法
     * @param <T> 类型
     */
    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> void putPresetObj(Class<T> type, Supplier<T> supplier) {
        if (type != null && supplier != null)
            PRESET_VALUES_MAP.put(type, (Supplier<Object>) supplier);
    }

    /**
     * 将value转换为type的类型的实例
     * @param value 要被转换的值，这个值一般是字符串类型
     * @param classType 目标对象的类型
     * @param genericType 这个目标类型的泛型信息
     * @return 生成好的对象，假如不能生成，返回null。可能抛出运行时异常
     * @throws Exception 解析数组时 {@link #resolveArray(Object, Class, Type)}
     */
    public static Object resolveArgument(Object value, Class<?> classType, Type genericType) throws Exception {
        if (classType.isArray() || ClassUtils.isExtendForm(classType, Collection.class)) {
            return resolveArray(value, classType, genericType);
        } else {
            return simpleTrans(String.valueOf(value), classType);
        }
    }

    /**
     * 简易的转换，转换基本类型
     * 根据占位符信息获取变量值，
     * @param value 一般是字符串
     * @param type 目标类型
     * @return 转换结果
     */
    public static Object simpleTrans(Object value, Class<?> type) {
        // 尝试进行占位符替换
        Optional<Object> pObj = resolvePlaceholder(value, type);
        if (pObj.isPresent())
            return pObj.get();

        Function<String, Object> convertor = STR_RESOLVE_MAP.get(type);
        if (convertor != null) {
            return convertor.apply(String.valueOf(value));
        } else {
            // 处理表单
            try {
                FormHelper.ObjWrapper wrapper = FormHelper.checkAndGet(type);
                if (wrapper.success)
                    return wrapper.instance;
                return DEFAULT_VALUE_MAP.getOrDefault(type, null);
            } catch (Exception e) {
                e.printStackTrace();
                return DEFAULT_VALUE_MAP.getOrDefault(type, null);
            }
        }
    }

    // 添加某类型的解析器，@Cmd的type=Parser的方法会被调用到这里来
    protected static void addParser(Function<String, Object> parser, Class<?> ... types) {
        if (parser == null || types == null || types.length == 0)
            return;
        for (Class<?> type : types) {
            if (type == null)
                continue;
            STR_RESOLVE_MAP.put(type, parser);
        }
    }

    // 解析数组和集合
    private static Object resolveArray(Object value, Class<?> type, Type genericType)
            throws ClassNotFoundException {
        if (type.isArray()) { // 数组
            return ClassUtils.genArray(type.getComponentType(), (String) value);
        } else {
            if (ClassUtils.isExtendForm(type, Set.class)) { // 集
                return ClassUtils.genSet(ClassUtils.getRawType(genericType).get(0), (String) value);
            } else if (ClassUtils.isExtendForm(type, List.class)) { // 列表
                return ClassUtils.genList(ClassUtils.getRawType(genericType).get(0), (String) value);
            } else {
                throw new RuntimeException("暂不支持的数据结构: " + type.getName());
            }
        }
    }

    /**
     * 占位符替换
     * <p>满足要求：确实存在变量，且变量的类型和方法参数类型一致</p>
     * @param value 占位符信息
     * @param type 目标类型
     * @return 处理结果，假如处理失败，则返回空
     */
    private static Optional<Object> resolvePlaceholder(Object value, Class<?> type) {
        if (!(value instanceof String))
            return Optional.empty();
        String key = (String) value;
        if (!key.startsWith(VariableManager.placeholder))
            return Optional.empty();
        int keyId = Integer.parseInt(key.substring(VariableManager.placeholder.length()));
        ReplacementRecord replacementRecord = Interpreter.getCurrentUser().getResources().getReplacementRecord();
        Optional<Object> placeholderObj = VariableManager.get(replacementRecord, keyId);
        if (placeholderObj.isPresent() && placeholderObj.get().getClass() == type) {
            return placeholderObj;
        }
        return Optional.empty();
    }

    private static void putDefVal(Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(String.class, null);
        STR_RESOLVE_MAP.put(String.class, convertor);
    }

    private static void putDefVal(Class<?> type1, Class<?> type2, Object value,
                                  Function<String, Object> convertor) {
        DEFAULT_VALUE_MAP.put(type1, value);
        DEFAULT_VALUE_MAP.put(type2, value);

        STR_RESOLVE_MAP.put(type1, convertor);
        STR_RESOLVE_MAP.put(type2, convertor);
    }

}
