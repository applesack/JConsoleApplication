package xyz.scootaloo.console.app.support.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/30 20:16
 */
public class StringUtils {

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

    public static List<String> toList(String line) {
        return Stream.of(line)
                .flatMap(ine -> Arrays.stream(ine.split(" ")))
                .collect(Collectors.toList());
    }

    public static String trimBothEnds(String line) {
        int len = line.length() - 1;
        StringBuilder sb = new StringBuilder(Math.max(len, 0));
        for (int i = 1; i<len; i++) {
            sb.append(line.charAt(i));
        }
        return sb.toString();
    }

}
