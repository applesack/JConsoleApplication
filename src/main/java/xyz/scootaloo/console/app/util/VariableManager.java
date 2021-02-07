package xyz.scootaloo.console.app.util;

import xyz.scootaloo.console.app.common.ResourceManager;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 变量管理器，如果不需要这个功能则在设置中关闭
 * set get echo 占位符替换
 * @author flutterdash@qq.com
 * @since 2021/1/14 20:19
 */
public abstract class VariableManager {
    // 需要启动此功能，需要在设置中开启
    public static final String msg = "设置中已关闭此功能";
    public static final String placeholder = "@#@";

    /**
     * 属性集，假如value是一个对象，那么key应该用符合拼写习惯的小驼峰写法，
     * {@code "student" -> new Student} 在引用的时候
     * {@code the name is ${student.name}}
     */
    private static final Map<String, Object> properties = new HashMap<>(16);
    private static final String DFT_RAND_STR = "0";
    private static final Random rand = ResourceManager.getRandom();

    // getter and setter ---------------------------------------------------------------------------

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

    public static Object get(String key) {
        return properties.get(key);
    }

    public static Object get() {
        if (!KVPairs.hisKVs.isEmpty()) {
            return KVPairs.hisKVs.peek().value;
        }
        return null;
    }

    public static Map<String, Object> getKVs() {
        return properties;
    }

    //----------------------------------------------------------------------------------------------

    public static void doClear() {
        KVPairs.hisKVs.clear();
    }

    /**
     * 将字符串中的占位符替换成Properties中的value
     * @param text 文本
     * @return 替换占位符后的文本
     */
    public static String resolvePlaceholders(String text) {
        KVPairs curKV = new KVPairs();
        StringBuilder sb = new StringBuilder();
        boolean isOpen = false;
        int lSign = -1;

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
                String key = String.valueOf(text.subSequence(lSign, i));
                curKV.key = key;
                String value = getValue(key, "${"+ key + "}", curKV);
                sb.append(value);
                continue;
            }
            if (!isOpen)
                sb.append(c);
        }
        if (curKV.hasVar)
            KVPairs.hisKVs.push(curKV);
        return sb.toString();
    }

    private static String getValue(String key, String defaultValue, KVPairs curKV) {
        Object value = properties.get(key);
        curKV.hasVar = true;
        if (value != null) {
            if (value instanceof String) {
                curKV.value = value;
                return value.toString();
            } else {
                curKV.value = value;
                return placeholder;
            }
        }
        String[] fields = key.split("\\.");
        if (fields.length < 2) {
            curKV.hasVar = false;
            return defaultValue;
        }
        if (fields[0].toLowerCase(Locale.ROOT).equals("rand"))
            return doRandom(fields[1], curKV);

        Object obj = properties.get(fields[0]);
        if (obj == null)
            return defaultValue;
        String[] res = new String[1];
        if (dfs(obj, fields, 1, res, curKV))
            return res[0];
        else
            return defaultValue;
    }

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
        public static final Stack<KVPairs> hisKVs = new Stack<>();

        // 是否有变量: 做为存储到集合的依据，假如占位符中包含的key没有对应的值，则为false
        private boolean hasVar;

        // 此变量的key
        public String key;

        // 此变量的值
        public Object value;

    }

}
