package xyz.scootaloo.console.app.util;

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

    private static final String[] BOX = {"0", "00", "000"};

    // 日期格式化
    private static final SimpleDateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");   // 时:分:秒
    private static final SimpleDateFormat SS_MS = new SimpleDateFormat("ss_SSS+");       // 秒:毫秒

    // 获取时间的时分秒格式，并加入到 stringBuilder
    public static StringBuilder getHourMinuteSecond(long timestamp, StringBuilder sb) {
        sb.append(HH_MM_SS.format(new Date(timestamp)));
        return sb;
    }

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

    public static String trimNumberSizeTo4(long num) {
        String nStr = String.valueOf(num);
        if (nStr.length() > 4)
            return "9999";
        if (nStr.length() == 4)
            return nStr;
        int lack = 4 - nStr.length();
        return BOX[lack - 1] + nStr;
    }

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

    public static List<String> toList(String line) {
        return Stream.of(line)
                .flatMap(ine -> Arrays.stream(ine.split(" ")))
                .collect(Collectors.toList());
    }

    public static String ignoreChar(String rawString, char igChar) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<rawString.length(); i++) {
            char c = rawString.charAt(i);
            if (c != igChar)
                sb.append(c);
        }
        return sb.toString();
    }

    public static boolean isNumber(String str) {
        for (int i = 0; i<str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLetter(c))
                return false;
        }
        return true;
    }

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

}
