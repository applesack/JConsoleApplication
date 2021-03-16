package xyz.scootaloo.test.parser;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.parser.preset.PresetFactoryManager;
import xyz.scootaloo.console.app.util.ParserTester;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/8 10:41
 */
public class TestCollectionParser {

    public static void main(String[] args) {
        PresetFactoryManager.getParserByName("collection").ifPresent(parser -> {
            ParserTester.createTest(parser, "test")
                    .addTestCommand("map{a:12, ' x b':'13 '} list:[1, 2, 4, 5] set:(1,2,1,4,5) arr=>[12, 34, 5]")
                    .addTestCommand("{a:12, ' x b':'13 '}")
                    .test();
        });
    }

    @Test
    public void testMethod() {
        Method method = ParserTester.getMethodByName("array");
        System.out.println(method);
    }

    public void test(@Opt(value = 'm', fullName = "map") Map<String, Integer> map,
                     @Opt(value = 'l', fullName = "list") List<Integer> list,
                     @Opt(value = 's', fullName = "set") Set<Long> set,
                     @Opt(value = 'a', fullName = "arr") Short[] arr) {
        System.out.println("--------------------------");
        System.out.println(map);
        System.out.println(list);
        System.out.println(set);
        System.out.println(Arrays.toString(arr));
    }

    public void array(Long[] longs, int[] arr) {

    }

}
