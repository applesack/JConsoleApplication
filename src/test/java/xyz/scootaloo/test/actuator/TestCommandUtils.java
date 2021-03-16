package xyz.scootaloo.test.actuator;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.parser.Actuator;
import xyz.scootaloo.console.app.support.Tester;

/**
 * 测试Actuator接口功能
 * @author flutterdash@qq.com
 * @since 2021/3/15 22:28
 */
public class TestCommandUtils {

    @Test
    public void testGetCommandName() {
        Tester.createTest(Actuator::getCommandName)
                .addCase("", "")
                .addCase(" ", "")
                .addCase("test", "test")
                .addCase(" test test ", "test")
                .addCase("t t", "t")
                .test();
    }

    @Test
    public void testGetCommandArgs() {
        Tester.createTest(Actuator::getCommandArgs)
                .addCase("", "")
                .addCase(" ", "")
                .addCase("test ", "")
                .addCase("test args", "args")
                .addCase("test args ", "args")
                .addCase("test arg1 arg2 ", "arg1 arg2")
                .addCase("test arg1 arg2", "arg1 arg2")
                .test();
    }

}
