package xyz.scootaloo.console.app.config;

import xyz.scootaloo.console.app.util.ClassUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 控制台最终的配置配置
 * @author flutterdash@qq.com
 * @since 2020/12/27 15:37
 */
public final class ConsoleConfig {
    // 应用信息
    private String appName;       // 应用名称
    private String prompt;        // 控制台提示
    private boolean printWelcome; // 是否输出欢迎信息
    private String[] exitCmd;     // 退出时使用的命令

    // 开发配置
    private int maxHistory;     // 最大保存历史记录的长度
    private boolean printStackTraceOnException; // 遇到异常时是否打印调用栈
    private Set<Supplier<Object>> factories;
    private List<String> initCommands;
    private boolean enableVariableFunction;

    // 扫描的基础包路径
    private String basePack;

    // 作者信息
    private Author author;

    public ConsoleConfig(ConsoleConfigProvider.DefaultValueConfigBuilder builder) {
        ClassUtils.copyProperties(builder, this);
    }

    // getter

    public String getAppName() {
        return this.appName;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public boolean isPrintWelcome() {
        return this.printWelcome;
    }

    public String[] getExitCmd() {
        return this.exitCmd;
    }

    public int getMaxHistory() {
        return this.maxHistory;
    }

    public boolean isPrintStackTraceOnException() {
        return this.printStackTraceOnException;
    }

    public Set<Supplier<Object>> getFactories() {
        return this.factories;
    }

    public List<String> getInitCommands() {
        return this.initCommands;
    }

    public boolean isEnableVariableFunction() {
        return this.enableVariableFunction;
    }

    public String getBasePack() {
        return this.basePack;
    }

    public Author getAuthor() {
        return this.author;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ConsoleConfig)) return false;
        final ConsoleConfig other = (ConsoleConfig) o;
        if (!other.canEqual(this)) return false;
        final Object this$appName = this.getAppName();
        final Object other$appName = other.getAppName();
        if (!Objects.equals(this$appName, other$appName)) return false;
        final Object this$prompt = this.getPrompt();
        final Object other$prompt = other.getPrompt();
        if (!Objects.equals(this$prompt, other$prompt)) return false;
        if (this.isPrintWelcome() != other.isPrintWelcome()) return false;
        if (!java.util.Arrays.deepEquals(this.getExitCmd(), other.getExitCmd())) return false;
        if (this.getMaxHistory() != other.getMaxHistory()) return false;
        if (this.isPrintStackTraceOnException() != other.isPrintStackTraceOnException()) return false;
        final Object this$factories = this.getFactories();
        final Object other$factories = other.getFactories();
        if (!Objects.equals(this$factories, other$factories)) return false;
        final Object this$initCommands = this.getInitCommands();
        final Object other$initCommands = other.getInitCommands();
        if (!Objects.equals(this$initCommands, other$initCommands))
            return false;
        if (this.isEnableVariableFunction() != other.isEnableVariableFunction()) return false;
        final Object this$basePack = this.getBasePack();
        final Object other$basePack = other.getBasePack();
        if (!Objects.equals(this$basePack, other$basePack)) return false;
        final Object this$author = this.getAuthor();
        final Object other$author = other.getAuthor();
        return Objects.equals(this$author, other$author);
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ConsoleConfig;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $appName = this.getAppName();
        result = result * PRIME + ($appName == null ? 43 : $appName.hashCode());
        final Object $prompt = this.getPrompt();
        result = result * PRIME + ($prompt == null ? 43 : $prompt.hashCode());
        result = result * PRIME + (this.isPrintWelcome() ? 79 : 97);
        result = result * PRIME + java.util.Arrays.deepHashCode(this.getExitCmd());
        result = result * PRIME + this.getMaxHistory();
        result = result * PRIME + (this.isPrintStackTraceOnException() ? 79 : 97);
        final Object $factories = this.getFactories();
        result = result * PRIME + ($factories == null ? 43 : $factories.hashCode());
        final Object $initCommands = this.getInitCommands();
        result = result * PRIME + ($initCommands == null ? 43 : $initCommands.hashCode());
        result = result * PRIME + (this.isEnableVariableFunction() ? 79 : 97);
        final Object $basePack = this.getBasePack();
        result = result * PRIME + ($basePack == null ? 43 : $basePack.hashCode());
        final Object $author = this.getAuthor();
        result = result * PRIME + ($author == null ? 43 : $author.hashCode());
        return result;
    }

    public String toString() {
        return "ConsoleConfig(appName=" + this.getAppName() + ", prompt=" + this.getPrompt() + ", printWelcome=" + this.isPrintWelcome() + ", exitCmd=" + java.util.Arrays.deepToString(this.getExitCmd()) + ", maxHistory=" + this.getMaxHistory() + ", printStackTraceOnException=" + this.isPrintStackTraceOnException() + ", factories=" + this.getFactories() + ", initCommands=" + this.getInitCommands() + ", enableVariableFunction=" + this.isEnableVariableFunction() + ", basePack=" + this.getBasePack() + ", author=" + this.getAuthor() + ")";
    }

}
