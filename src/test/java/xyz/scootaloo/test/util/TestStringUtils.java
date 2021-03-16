package xyz.scootaloo.test.util;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.support.Tester;
import xyz.scootaloo.console.app.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/16 13:29
 */
public class TestStringUtils {

    @Test
    public void testSplit() {
        Tester.createTest((Function<String, List<String>>) StringUtils::split)
                .addCase("12 23 34", getList("12", "23", "34"))
                .addCase(" 12  23  34", getList("12", "23", "34"))
                .setMatcher((list1, list2) -> {
                    if (list1.size() != list2.size())
                        return false;
                    int size = list1.size();
                    for (int i = 0; i<size; i++) {
                        if (!list1.get(i).equals(list2.get(i)))
                            return false;
                    }
                    return true;
                }).test();
    }

    public static List<String> getList(String ... strings) {
        return Arrays.asList(strings.clone());
    }

}
