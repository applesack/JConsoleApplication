package xyz.scootaloo.test.app;

import xyz.scootaloo.console.app.ApplicationRunner;
import xyz.scootaloo.console.app.anno.Cmd;

import java.util.List;
import java.util.Set;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/8 17:38
 */
public class TestArrays {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

    @Cmd
    public void say(List<Set<String>> strings) {
        System.out.println(strings);
    }

    @Cmd
    public int getNumber(Integer number) {
        return number;
    }

    @Cmd
    public Integer showNumber(int number) {
        System.out.println("show: " + number);
        return number;
    }

    @Cmd
    public String xx() {
        return "12x";
    }

}
