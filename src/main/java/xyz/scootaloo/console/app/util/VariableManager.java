package xyz.scootaloo.console.app.util;

import xyz.scootaloo.console.app.common.ResourceManager;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 变量管理器，如果不需要这个功能则在设置中关闭
 * <p>这里维护了一个一个HashMap做为变量池，可以通过命令将一个方法的返回值绑定到一个变量名上。</p>
 * <p>然后这些变量可以用占位符的方式在其他命令行中使用，占位符最终会替换成变量的实际的值。</p>
 * @author flutterdash@qq.com
 * @since 2021/1/14 20:19
 */
public final class VariableManager {
    /* 需要启动此功能，需要在设置中开启 */
    public static final String msg = "设置中已关闭此功能";
    public static final String placeholder = "@#@";

    /**
     * 变量名是一个字符串； 变量值可以是任意对象
     * {@code "student" -> new Student} 在引用的时候
     * {@code the name is ${student.name}}
     */
    private static final Map<String, Object> properties = new HashMap<>(16);
    private static final String DFT_RAND_STR = "0";
    private static final Random rand = ResourceManager.getRandom();
    private static int keyId = 0;

    // getter and setter ---------------------------------------------------------------------------

    /**
     * 向变量池中放置键值对，使用方式和HashMap一致
     * <p>注意: 如果键是小数点 '.', 则整个变量池都会被清空。<br>
     * 如果值是小数点 '.', 则这个变量会被从变量池中移除</p>
     * @param key 变量名
     * @param value 变量值
     */
    public static void set(String key, Object value) {
        if (key.startsWith(".")) {
            properties.clear();
            return;
        }
        if (value.equals("."))
            properties.remove(key);
        else
            properties.put(key, value);
    }

    /**
     * 根据变量名获取变量获取变量
     * @param key 变量值
     * @return Optional对象
     */
    public static Optional<Object> get(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    /**
     * 根据id获取历史替换记录
     * @see xyz.scootaloo.console.app.parser.TransformFactory#simpleTrans(Object, Class) 使用点
     * @param tKeyId 这条替换记录的id
     * @return 返回这个变量值，假如没有这个id的信息，则返回空。
     */
    public static Optional<Object> get(int tKeyId) {
        Optional<KVPairs> rsl = KVPairs.hisKVs.stream()
                .filter(kvPairs -> kvPairs.id == tKeyId)
                .findAny();
        if (rsl.isPresent()) {
            Object value = rsl.get().value;
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return 返回整个变量池的信息
     */
    public static Map<String, Object> getKVs() {
        return properties;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 清除上一次解析命令行的占位符替换记录
     */
    public static void reset() {
        keyId = 0;
        KVPairs.hisKVs.clear();
    }

    /**
     * 将字符串中的占位符替换成Properties中的value
     * @see xyz.scootaloo.console.app.parser.preset.SystemPresetCmd#onResolveInput 使用点
     * 这里当命令行参数按照空格分段以后，每个部分假如有占位符，都会被替换成变量的“@#@”加上id，同时每次替换都会在 {@link KVPairs} 做记录。
     * @param text 文本
     * @return 替换占位符后的文本
     */
    public static String resolvePlaceholders(String text) {
        KVPairs curKV = new KVPairs();
        StringBuilder sb = new StringBuilder();
        boolean isOpen = false;
        int lSign = -1;

        // 遍历字符串，检查其中的占位符
        for (int i = 0; i<text.length(); i++) {
            char c = text.charAt(i);
            if (c == '$') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '{') {

                    if (isOpen && lSign - 2 >= 0)
                        for (int j = lSign - 2; j<i; j++)
                            sb.append(text.charAt(j));

                    isOpen = true;
                    lSign = i + 2;
                    i += 1;
                    continue;
                }
            }
            if (isOpen && c == '}') {
                isOpen = false;
                if (lSign == i)
                    continue;
                // 获取占位符中的内容
                String key = String.valueOf(text.subSequence(lSign, i));
                // 这段内容做为变量的key
                curKV.key = key;
                // 尝试在变量池中找到对应的值，假如没有则保持原样
                String value = getValueOrDefault(key, "${"+ key + "}", curKV);
                sb.append(value);
                continue;
            }
            if (!isOpen)
                sb.append(c);
        }
        if (curKV.hasVar) {
            curKV.id = keyId;
            KVPairs.hisKVs.add(curKV);
            keyId++;
        }
        return sb.toString();
    }

    /**
     * @param key 这是占位符中包含的内容，做为key
     * @param defaultValue 假如不能找到key对应的值，则返回defaultValue
     * @param curKV 替换过程中的信息会记录到这个对象中
     * @return 参考defaultValue的描述
     */
    private static String getValueOrDefault(String key, String defaultValue, KVPairs curKV) {
        // 从变量池中尝试查找这个键
        Object value = properties.get(key);
        curKV.hasVar = true;
        if (value != null) {
            // 键不为空，且是String类型，直接返回
            if (value instanceof String) {
                curKV.value = value;
                return value.toString();
            }
            // 是一个非字符串类型的对象，这里返回占位符，交给其他使用者处理
            else {
                curKV.value = value;
                return getPlaceholderWithId();
            }
        }

        String[] fields = key.split("\\.");
        if (fields.length < 2) {
            // 将这个key按照小数点分隔后，所得到的位数小于2，这不符合预期的格式，所以直接返回
            curKV.hasVar = false;
            return defaultValue;
        }
        // 假如这段字符串类似这样的格式 "rand.int(1,2)"，则按照随机函数的方式处理
        if (fields[0].toLowerCase(Locale.ROOT).equals("rand"))
            return doRandom(fields[1], curKV);

        // 它可能是一个对象中的一个属性，进行递归操作找到这个目标属性值
        Object obj = properties.get(fields[0]);
        if (obj == null)
            return defaultValue;
        String[] res = new String[1];
        if (dfs(obj, fields, 1, res, curKV))
            return getPlaceholderWithId();
        else
            // 查找失败，返回默认值
            return defaultValue;
    }

    /**
     * 根据字符串获得随机值
     * @param option 随机标识，目前只支持 bool 或者 int, 返回这两种类型的随机值
     * @param curKV 替换过程中的信息会记录到这个对象中
     * @return 随机值代表的字符串
     */
    private static String doRandom(String option, KVPairs curKV) {
        option = option.toLowerCase(Locale.ROOT);
        if (option.startsWith("int")) {
            option = option.substring(3);
            if (option.isEmpty()) {
                int res = rand.nextInt();
                return setAndReturn(res, curKV);
            } else {
                if (option.startsWith("(") && option.endsWith(")")) {
                    option = StringUtils.trimBothEnds(option);
                    if (option.isEmpty()) {
                        curKV.value = 0;
                        return DFT_RAND_STR;
                    } else {
                        String[] segment = option.split(",");
                        if (segment.length == 0 || segment.length > 2) {
                            curKV.value = 0;
                            return DFT_RAND_STR;
                        }
                        int low = Integer.parseInt(segment[0]);
                        if (segment.length == 1) {
                            int res = rand.nextInt() + low;
                            return setAndReturn(res, curKV);
                        } else {
                            int height = Integer.parseInt(segment[1]);
                            int res = rand.nextInt(height - low) + low;
                            return setAndReturn(res, curKV);
                        }
                    }
                } else {
                    throw new RuntimeException("不能识别开闭符号: " + option);
                }
            }
        } else if (option.startsWith("bool")) {
            boolean res = rand.nextInt() % 2 == 0;
            return setAndReturn(res, curKV);
        } else {
            throw new RuntimeException("不支持的随机功能");
        }
    }

    private static String setAndReturn(Object res, KVPairs curKV) {
        curKV.value = res;
        return String.valueOf(res);
    }

    private static String getPlaceholderWithId() {
        return placeholder + keyId;
    }

    private static boolean dfs(Object obj, String[] fields, int idx, String[] res, KVPairs curKV) {
        if (idx == fields.length) {
            curKV.value = obj;
            res[0] = obj.toString();
            return true;
        }

        Class<?> clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fields[idx]);
            field.setAccessible(true);
            Object fieldObj = field.get(obj);
            return dfs(fieldObj, fields, idx + 1, res, curKV);
        } catch (Exception e) {
            return false;
        }
    }

    // 命令行中被替换的键值对，在这里记录，每次解析新的命令行的时候，之前的记录会被清除
    public static class KVPairs {

        // 一条命令中如果有多个占位符，那么它们按照顺序存储在这个集合中
        public static final Set<KVPairs> hisKVs = new LinkedHashSet<>();

        // 是否有变量: 做为存储到集合的依据，假如占位符中包含的key没有对应的值，则为false
        private boolean hasVar;

        // 此变量的key
        public String key;

        // 此变量的值
        public Object value;

        // 标记
        public Integer id;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KVPairs kvPairs = (KVPairs) o;
            return Objects.equals(id, kvPairs.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

}
