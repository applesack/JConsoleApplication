package xyz.scootaloo.console.app.support.config;

import lombok.Getter;
import lombok.ToString;
import xyz.scootaloo.console.app.support.component.AppType;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:37
 */
@Getter
@ToString
public class ConsoleConfig {
    // 应用类型
    private AppType appType;

    // 应用信息
    private String appName; // 应用名称
    private String prompt;  // 控制台提示
    private String[] exitCmd; // 退出时使用的命令
    private Class<?> bootClazz;
    private int maxHistory;

    // 扫描的基础包路径
    private String basePack;

    // 作者信息
    private String author;
    private String email;
    private String date;

    public ConsoleConfig(ConfigProvider.DefaultValueConfigBuilder builder) {
        ClassUtils.copyProperties(builder, this);
    }

}
