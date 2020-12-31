package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.component.CommandFactory;
import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.component.Plugin;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;
import xyz.scootaloo.console.app.support.plugin.ConsolePluginAdapter;

import java.util.List;

/**
 *   #####################################
 *  # 提示: 此类可以删除或者修改，不影响系统功能 #
 * ######################################
 *
 * ------ 插件 ------
 * 更准确的说法应该是拦截器，
 * 在系统运行的不同节点，注册的方法即被调用。
 * 可以通过实现 ConsolePlugin 接口来补充
 * ConsolePlugin 有一个适配器接口 ConsolePluginAdapter，可以通过实现这个适配器来完成插件的开发。
 * 系统中有一个默认的插件，实现历史记录功能和占位符替换功能 {@link xyz.scootaloo.console.app.support.parser.SystemPresetCmd}
 * -----------------
 * @see xyz.scootaloo.console.app.support.plugin.ConsolePlugin
 *
 * @author flutterdash@qq.com
 * @since 2020/12/30 23:06
 */
@Plugin(enable = false) // 修改为true启动本插件
@CommandFactory(enable = false) // 修改为true启用命令工厂
public class PluginDemo implements ConsolePluginAdapter {

    /**
     * 每个插件都需要有一个名字，
     * 可以通过下面几个命令查看系统中的插件
     *      plgs             --- 查看系统中的插件
     *      dis [插件名]      --- 停用某插件
     *      en [插件名]       --- 重新启用被停用的插件
     * @return 插件的名字
     */
    @Override
    public String getName() {
        return "myPlg";
    }

    /**
     * 通过 plgs 命令可以查看到
     * @return 返回插件的描述
     */
    @Override
    public String info() {
        return "插件测试 -";
    }

    /**
     * 当系统内有多个插件，order值决定方法的调用顺序
     * @return 优先级，数值越小越先执行
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 此方法被调用时，系统会传进来一个代表运行时机的枚举对象，
     * 枚举对象的含义请参考:
     * @see xyz.scootaloo.console.app.support.plugin.ConsolePlugin
     * @param moment 系统调用插件方法的时机
     * @return 如果此时返回true，则此时的方法被调用
     */
    @Override
    public boolean accept(Moment moment) {
        // 假如表示对系统启动事件感兴趣可以这样写，同时重写onAppStarted方法
        // return moment == Moment.OnAppStarted
        return true; // 这里为了方便演示，直接返回 true 表示对所有事件都感兴趣，需要重写所有方法
    }

    /**
     * @see xyz.scootaloo.console.app.support.plugin.ConsolePlugin
     * @param config -
     */
    @Override
    public void onAppStarted(ConsoleConfig config) {
        System.out.println("项目启动");
        System.out.println(config);
    }

    @Override
    public String onInput(String cmdline) {
        System.out.println("接收命令" + cmdline);
        return cmdline;
    }

    @Override
    public void onResolveInput(String cmdName, List<String> cmdItems) {
        System.out.println("处理命令之前" + cmdName + cmdName);
    }

    @Override
    public void onInputResolved(String cmdName, Object rtnVal) {
        System.out.println("命令名:[" + cmdName + "], 返回值:" + rtnVal);
    }

}
