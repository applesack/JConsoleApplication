package xyz.scootaloo.console.app.util;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串工具
 * @author flutterdash@qq.com
 * @since 2020/12/30 20:16
 */
public final class StringUtils {
    // 用于给指定长度的字符串补齐
    private static final String[] BOX = {"0", "00", "000"};

    // 日期格式化
    private static final SimpleDateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");   // 时:分:秒
    private static final SimpleDateFormat SS_MS = new SimpleDateFormat("ss_SSS+");       // 秒:毫秒

    // 获取时间的时分秒格式，并加入到 stringBuilder
    public static StringBuilder getHourMinuteSecond(long timestamp, StringBuilder sb) {
        sb.append(HH_MM_SS.format(new Date(timestamp)));
        return sb;
    }

    /**
     * 得到一个格式化后的字符串
     * @param interval 一段时间，单位: 毫秒
     * @param sb 结果将填充到这个StringBuilder中
     */
    public static void getIntervalBySS_MS(long interval, StringBuilder sb) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(SS_MS.format(new Date(interval)));
        for (int i = 0; i<tmp.length(); i++) {
            char c = tmp.charAt(i);
            if (c == '_') {
                sb.append('s');
            } else if (c == '+') {
                sb.append("ms");
            } else {
                sb.append(c);
            }
        }
    }

    // 对于 word 中，'-'符号之前的内容进行小写处理
    public static String customizeToLowerCase0(String word) {
        StringBuilder stringBuilder = new StringBuilder();
        int len = word.length();
        for (int i = 0; i<len; i++) {
            char c = word.charAt(i);
            if (c == '-') {
                for (; i<len; i++) {
                    c = word.charAt(i);
                    stringBuilder.append(c);
                }
                break;
            } else {
                stringBuilder.append(Character.toLowerCase(c));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 修剪一个字符串长度到 7 <br>
     * 当目标字符串大于这个长度，后三位替换为 '.' <br>
     * 当目标字符串小于这个长度，缺少的位置填充为 '_'
     * @param str 要被修剪的字符串
     * @return 修剪后的字符串
     */
    public static String trimSizeTo7(String str) {
        char[] rslChars = new char[7];
        int minSize = Math.min(str.length(), 7);
        for (int i = 0; i<minSize; i++) {
            rslChars[i] = str.charAt(i);
        }
        for (int i = minSize; i<7; i++)
            rslChars[i] = '_';
        if (str.length() > 7) {
            for (int i = 5; i<7; i++)
                rslChars[i] = '.';
        }
        return new String(rslChars);
    }

    /**
     * 修剪只包含数字的字符串, 截取最后4位<br>
     * 假如字符串大于4位，则返回 “9999” <br>
     * 恰好四位则直接返回<br>
     * 少于4位用0补齐
     * @param num 一个数字
     * @return 修剪后的字符串
     */
    public static String trimNumberSizeTo4(long num) {
        String nStr = String.valueOf(num);
        if (nStr.length() > 4)
            return "9999";
        if (nStr.length() == 4)
            return nStr;
        int lack = 4 - nStr.length();
        return BOX[lack - 1] + nStr;
    }

    /**
     * 将字符串按照空格分隔，拼接成列表
     * @param line 一个字符串
     * @return 列表结果
     */
    public static List<String> toList(String line) {
        return Stream.of(line)
                .flatMap(ine -> Arrays.stream(ine.split(" ")))
                .collect(Collectors.toList());
    }

    /**
     * 忽略字符串中的某个字符。<br>
     * 假如有一个字符串 {@code str = "hello world"} <br>
     * 执行方法 {@code ignoreChar(str, 'o')} 后，得到结果:
     * {@code "hell wrld"}
     * @param rawString 原字符串
     * @param igChar 要忽略的某字符
     * @return 结果
     */
    public static String ignoreChar(String rawString, char igChar) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<rawString.length(); i++) {
            char c = rawString.charAt(i);
            if (c != igChar)
                sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 简易的判断逻辑，不包含字符即认为是数字
     * @param str 字符串
     * @return 是否是数字
     */
    public static boolean isNumber(String str) {
        for (int i = 0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLetter(c))
                return false;
        }
        return true;
    }

    /**
     * 减去字符串两端的字符
     * @param str 原字符串
     * @return 余下的字符串
     */
    public static String trimBothEnds(String str) {
        if (str.length() <= 1)
            return str;
        char[] rslChars = new char[str.length() - 2];
        for (int i = 1; i<str.length() - 1; i++)
            rslChars[i - 1] = str.charAt(i);
        return new String(rslChars);
    }

    public static String getPack(String className) {
        StringBuilder tmp = new StringBuilder();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i<className.length(); i++) {
            char c = className.charAt(i);
            if (c == '.') {
                res.append(tmp.toString());
                res.append('.');
                tmp.setLength(0);
            } else {
                tmp.append(c);
            }
        }
        if (res.charAt(res.length() - 1) == '.') {
            res.setLength(res.length() - 1);
        }
        return res.toString();
    }

    // 反射操作，查看方法的参数和返回值信息

    /**
     * 获取精简的类型表示<br>
     * 输入 {@code "java.util.Map<java.lang.String,java.lang.Integer>"}
     * 输出 {@code "Map<String,Integer>"}
     * @param type 类型，通常是方法的参数，或者是方法的返回值
     * @return 精简的类型
     */
    public static String typeSimpleView(Type type) {
        return typeSimpleView(type.toString());
    }

    public static String typeSimpleView(String typeStr) {
        typeStr = typeStr.replace("class ", "");
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i<typeStr.length(); i++) {
            char c = typeStr.charAt(i);
            if (c == ' ' || c == ';')
                continue;
            tmp.append(c);
        }
        typeStr = tmp.toString();
        if (typeStr.indexOf('<') == -1) {
            return getSimpleName(typeStr);
        }
        tmp.setLength(0);
        StringBuilder rsl = new StringBuilder();

        for (int i = 0; i<typeStr.length(); i++) {
            char c = typeStr.charAt(i);
            if (c == '<' || c == '>' || c == ',') {
                if (tmp.length() != 0 && tmp.charAt(tmp.length() - 1) == ';')
                    tmp.setLength(tmp.length() - 1);
                rsl.append(getSimpleName(tmp.toString())).append(c);
                tmp.setLength(0);
            } else {
                tmp.append(c);
            }
        }
        if (tmp.length() > 0)
            rsl.append(tmp);
        return rsl.toString();
    }

    private static String getSimpleName(String str) {
        boolean isArray = str.startsWith("[");
        int pointIdx = str.lastIndexOf(".");
        if (pointIdx == -1 || pointIdx == str.length() - 1) {
            return str + (isArray ? "[]" : "");
        } else {
            return str.substring(pointIdx + 1) + (isArray ? "[]" : "");
        }
    }

}
