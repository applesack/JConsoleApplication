package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.common.Factory;
import xyz.scootaloo.console.app.parser.preset.SystemPresetCmd;

/**
 * 标记此接口 并注册到系统后，系统将会扫描其中的方法并处理
 * @see SystemPresetCmd.SystemCommandHelp 此接口在框架中的应用
 * @author flutterdash@qq.com
 * @since 2021/1/20 11:42
 */
public interface HelpDoc extends Factory {

}
