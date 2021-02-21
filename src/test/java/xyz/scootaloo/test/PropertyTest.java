package xyz.scootaloo.test;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Properties;

/**
 * @author flutterdash@qq.com
 * @since 2021/2/16 21:29
 */
public class PropertyTest {

    public static void main(String[] args) {
        Properties properties = System.getProperties();
        System.out.println(properties.getProperty("user.name"));
    }

    @Test
    public void test() {
        Calendar calendar = Calendar.getInstance();
        System.out.println(calendar.get(Calendar.HOUR_OF_DAY));
    }

}
