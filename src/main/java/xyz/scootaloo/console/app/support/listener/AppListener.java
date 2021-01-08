package xyz.scootaloo.console.app.support.listener;

import xyz.scootaloo.console.app.support.component.Moment;
import xyz.scootaloo.console.app.support.config.ConsoleConfig;

import java.util.List;

/**
 * 插件接口
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:30
 */
public interface AppListener {

    /**
     *
     * @return 是否启用
     */
    boolean enable();

    /**
     *
     * @return 返回插件的名字
     */
    String getName();

    /**
     *
     * @return 返回插件的优先级
     */
    int getOrder();

    /**
     *
     * @param moment 当前系统运行到的节点
     * @return 是否需要监听这个事件
     */
    boolean accept(Moment moment);

    /**
     *
     * @param config 系统装配完命令，已获取配置类，监听此事件可以获取这个配置类
     */
    void onAppStarted(ConsoleConfig config);

    /**
     *
     * @param cmdline 系统接收到键盘输入的命令，但还未开始处理，监听此事件的插件可以返回自行修改后的命令
     * @return 可以自行修改命令行内容，或者不修改直接返回
     */
    String onInput(String cmdline);

    /**
     *
     * @param cmdName 当前准备处理的命令的名字
     * @param cmdItems 此命令的参数，已按照空格分隔，此时命令还未开始解析，这个时候仍然可以修改键盘输入的命令
     */
    void onResolveInput(String cmdName, List<String> cmdItems);

    /**
     *
     * @param cmdName 当前准备处理的命令的名字
     * @param rtnVal 命令执行执行的返回值，假如命令无返回值或者命令执行过程中抛出异常，则得到的值是null
     */
    void onInputResolved(String cmdName, Object rtnVal);

    /**
     *
     * @return 返回此插件的描述
     */
    default String info() { return ""; }

}
