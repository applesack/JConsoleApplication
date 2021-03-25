package xyz.scootaloo.test.parser;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.parser.preset.PresetFactoryManager;
import xyz.scootaloo.console.app.util.ParserTester;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/25 13:58
 */
public class SubParserTest {

    @Test
    public void testSub() {
        PresetFactoryManager.getParserByName("sub")
                .ifPresent(parser ->
                        ParserTester.createTest(parser, "run")
                                .addTestCommand("  ")
                                .addTestCommand("p1")
                                .addTestCommand("p p2")
                                .addTestCommand("pack p3")
                                .test());

    }

    private void run(@Opt(value = 'p', fullName = "pack", dftVal = "leetcode") String pack) {
        System.out.println(pack);
    }

}
