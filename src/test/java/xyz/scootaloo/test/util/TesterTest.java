package xyz.scootaloo.test.util;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.support.Tester;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/23 13:59
 */
public class TesterTest {

    @Test
    public void test() {
        int[] input = {1, 2, 3, 4, 5};
        Tester.createTest(this::getInts)
                .addCase(input, input)
                .test();
    }

    private int[] getInts(int[] arr) {
        return arr;
    }

}
