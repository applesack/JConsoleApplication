package xyz.scootaloo.test.parser;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.parser.DftParameterParser;
import xyz.scootaloo.console.app.parser.ParameterParser;
import xyz.scootaloo.console.app.util.ParserTester;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/16 12:00
 */
public class TestDefaultParser {

    @Test
    public void test() {
        ParameterParser parser = new DftParameterParser();
        ParserTester.createTest(parser, "find")
                .addTestCommand("-t sys")
                .addTestCommand("-t sys --name find")
                .test();
    }

    private void find(@Opt(value = 's', fullName = "name") String name,
                      @Opt(value = 't', fullName = "tag") String tag) {
        System.out.println("--------------------");
        System.out.println("name: " + name);
        System.out.println("tag: " + tag);
    }

}
