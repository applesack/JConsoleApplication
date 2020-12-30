package xyz.scootaloo.console.app.support.utils;

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

}
