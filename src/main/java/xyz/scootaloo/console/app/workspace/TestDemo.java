package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.plugin.ConsolePluginAdapter;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
@CommandFactory
public class TestDemo implements Colorful{

    @Cmd(type = CmdType.Init)
    private void init() {
        println("启动");
    }

    @Cmd
    private void add(int a, int b) {
        println(a + b);
    }

    @Cmd
    private void run(String imageName, @Opt('d') boolean d, @Opt('i') boolean i, @Opt('t') boolean t,
                                        @Req("name") String name, @Opt('v') String pathMapping,
                                        @Opt('p') String portMapping) {
        println("镜像名称 => " + imageName);
        println("参数 => " + d + i + t);
        println("容器名称 =>" + name);
        println("路径映射 => " + pathMapping);
        println("端口映射 =>" + portMapping);
    }

}
