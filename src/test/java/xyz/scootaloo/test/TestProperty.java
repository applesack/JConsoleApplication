package xyz.scootaloo.test;

import java.util.Properties;

/**
 * @author flutterdash@qq.com
 * @since 2021/2/16 11:41
 */
public class TestProperty {

    public static void main(String[] args) {
        System.out.println(getClearCommand());
    }

    private static String getClearCommand() {
        Properties prop = System.getProperties();
        String os = prop.getProperty("os.name");
        if (os != null && os.toLowerCase().contains("linux")) {
            return "clear";
        } else {
            return "cls";
        }
    }

}
