package xyz.scootaloo.console.app.support.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串工具
 * @author flutterdash@qq.com
 * @since 2020/12/30 20:16
 */
public abstract class StringUtils {

    private static final String[] BOX = {"0", "00", "000"};

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

    public static String ignoreChars(String rawString, char igChar) {
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

}
