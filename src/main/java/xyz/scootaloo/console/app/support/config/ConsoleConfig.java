package xyz.scootaloo.console.app.support.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.util.List;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:37
 */
@Getter
@ToString
@EqualsAndHashCode
public class ConsoleConfig {
    // 应用类型
    private AppType appType;

    // 应用信息
    private String appName;       // 应用名称
    private String prompt;        // 控制台提示
    private boolean printWelcome; // 是否输出欢迎信息
    private String[] exitCmd;     // 退出时使用的命令

    // 开发者配置
    private int maxHistory;     // 最大保存历史记录的长度
    private boolean printStackTraceOnException; // 遇到异常时是否打印调用栈
    private List<Class<?>> factories;
    private List<String> initCommands;

    // 扫描的基础包路径
    private String basePack;

    // 作者信息
    private Author author;

    public ConsoleConfig(ConsoleConfigProvider.DefaultValueConfigBuilder builder) {
        ClassUtils.copyProperties(builder, this);
    }

}
