package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.*;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
@StrategyFactory
public class TestDemo implements Colorful {

    @Cmd(type = CmdType.Init)
    private void init() {
        println("启动");
    }

//    @Cmd(type = CmdType.Pre)
//    private boolean flag() {
//        return true;
//    }

    @Cmd
    private void add( boolean c, @Req("q") int a, @Opt('c') int b) {
        println(a + b);
    }

}
