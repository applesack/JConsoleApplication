package xyz.scootaloo.console.app.listener;

import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ConsoleMessage;
import xyz.scootaloo.console.app.common.Factory;
import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.List;

/**
 * 应用事件监听器接口
 * <p>实现此接口后，需要重写 {@link #config(AppListenerProperty)} 方法，这个方法有一个参数是配置对象，
 * 可以用这个对象指定当前监听器感兴趣的事件信息。然后重写对应事件的方法。</p>
 * <p>注意：接口需要注册入框架后才能被框架识别。如何将实现类注册到框架请参考 {@link Console#factories()} </p>
 * @see xyz.scootaloo.console.app.parser.preset.SystemPresetCmd#config(AppListenerProperty) 框架中此接口方法的使用
 * @see AppListenerAdapter 适配器，需要使用监听器时，实现适配器接口即可
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:30
 */
public interface AppListener extends Factory {

    /**
     * @return 是否启用
     */
    boolean enable();

    /**
     * @return 返回插件的名字
     */
    String getName();

    /**
     * @param interested 编辑当前监听器感兴趣的事件，以及可以指定优先级
     */
    void config(AppListenerProperty interested);

    /**
     * @param config 系统装配完命令，已获取配置类，监听此事件可以获取这个配置类
     */
    void onAppStarted(ConsoleConfig config);

    /**
     * @param cmdline 系统接收到键盘输入的命令，但还未开始处理，监听此事件的插件可以返回自行修改后的命令
     * @return 可以自行修改命令行内容，或者不修改直接返回
     */
    String onInput(String cmdline);

    /**
     * @param cmdName 当前准备处理的命令的名字
     * @param cmdItems 此命令的参数，已按照空格分隔，此时命令还未开始解析，这个时候仍然可以修改键盘输入的命令
     */
    void onResolveInput(String cmdName, List<String> cmdItems);

    /**
     * @param cmdName 当前准备处理的命令的名字
     * @param info 命令执行执行的返回值包装，假如命令无返回值或者命令执行过程中抛出异常，则得到的值是null
     */
    void onInputResolved(String cmdName, InvokeInfo info);

    /**
     * @param message 控制台消息
     */
    void onMessage(ConsoleMessage message);

    /**
     * @return 返回此监听器的描述
     */
    default String info() { return ""; }

}
