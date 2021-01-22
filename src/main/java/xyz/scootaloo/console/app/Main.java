package xyz.scootaloo.console.app;

import xyz.scootaloo.console.app.anno.Boot;
import xyz.scootaloo.console.app.anno.Cmd;
import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.application.ApplicationRunner;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 14:58
 */
@Boot
public class Main {

    public static void main(String[] args) {
        ApplicationRunner.consoleApplication().run();
    }

    @Cmd
    public void s(@Opt(value = 'o', fullName = "who") String who,
                  @Opt(value = 'a', fullName = "action") String action,
                  @Opt(value = 'w',fullName = "what") String what,
                  @Opt(value = 'h', fullName = "how") String how) {
        System.out.println("主语: " + who);
        System.out.println("谓语: " + action);
        System.out.println("宾语: " + what);
        System.out.println("状语: \"" + how + "\"");
    }

}
