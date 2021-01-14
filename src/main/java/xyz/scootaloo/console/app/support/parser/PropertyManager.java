package xyz.scootaloo.console.app.support.parser;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * 属性管理器，如果不需要这个功能则在设置中关闭
 * set get 占位符替换
 * @author flutterdash@qq.com
 * @since 2021/1/14 20:19
 */
public class PropertyManager {

    protected static final String msg = "设置中已关闭此功能";
    protected static final String placeholder = "@#@";

    private static boolean hasObjValue = false;
    private static Object objValue = null;

    /**
     * 属性集，假如value是一个对象，那么key应该用符合拼写习惯的小驼峰写法，
     * {@code "student" -> new Student} 在引用的时候
     * {@code the name is ${student.name}}
     */
    private static final Map<String, Object> properties = new HashMap<>(16);
    private static final Random rand = new Random();

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
        if (hasObjValue) {
            Object rVal = objValue;
            objValue = null;
            hasObjValue = false;
            return rVal;
        }
        return null;
    }

    public static Map<String, Object> getKVs() {
        return properties;
    }

    //----------------------------------------------------------------------------------------------

    /**
     * 将字符串中的占位符替换成Properties中的value
     * @param text 文本
     * @return 替换占位符后的文本
     */
    public static String resolvePlaceholders(String text) {
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
                String value = getValue(key, "${"+ key + "}");
                sb.append(value);
                continue;
            }
            if (!isOpen)
                sb.append(c);
        }
        return sb.toString();
    }

    private static String getValue(String key, String defaultValue) {
        Object value = properties.get(key);
        if (value != null) {
            if (value instanceof String) {
                return value.toString();
            } else {
                hasObjValue = true;
                objValue = value;
                return placeholder;
            }
        }
        String[] fields = key.split("\\.");
        if (fields.length < 2)
            return defaultValue;
        if (fields[0].toLowerCase(Locale.ROOT).equals("rand"))
            return doRandom(fields[1]);

        Object obj = properties.get(fields[0]);
        if (obj == null)
            return defaultValue;
        String[] res = new String[1];
        if (dfs(obj, fields, 1, res))
            return res[0];
        else
            return defaultValue;
    }

    private static String doRandom(String option) {
        option = option.toLowerCase(Locale.ROOT);
        switch (option) {
            case "int":
                return String.valueOf(rand.nextInt());
            case "bool":
                return String.valueOf(rand.nextInt() % 2 == 0);
            default:
                throw new RuntimeException("不支持的选项: " + option);
        }
    }

    private static boolean dfs(Object obj, String[] fields, int idx, String[] res) {
        if (idx == fields.length) {
            res[0] = obj.toString();
            return true;
        }

        Class<?> clazz = obj.getClass();
        try {
            Field field = clazz.getDeclaredField(fields[idx]);
            field.setAccessible(true);
            Object fieldObj = field.get(obj);
            return dfs(fieldObj, fields, idx + 1, res);
        } catch (Exception e) {
            return false;
        }
    }

}
