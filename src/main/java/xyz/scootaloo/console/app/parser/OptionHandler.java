package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.common.Factory;

import java.util.List;

/**
 * 额外参数的处理器<br>
 * <p>一般情况下一条命令行由两个部分组成，{@code cmdName cmdArg} 命令名和参数。<br>
 * 在这个框架允许使用其他方式运行命令行。类似于这种格式 {@code cmdName-O**}，其中这个 'O' 代表一种操作, "**" 代表额外的参数值，<br>
 * 当你实现了当前这个的接口，并注册到了框架，这样当你输入一条命令行，比如 {@code test-Ops param} 时, 框架会查找名为 'O' 的参数处理器,<br>
 * 假如你的参数处理器的 {@link #option()} 方法恰好返回 'O'，
 * 框架下一步就会调用你的实现的 {@link #runWithParameter(String, String, List, Interpreter)} 方法, <br>
 * 同时把当前调用的命令行的信息，以及解释器传递进来，把执行的任务交给你来处理。</p>
 * <p>你可以通过实现这个接口，来处理某些命令的返回值，参考以下两种实现: </p>
 * @see xyz.scootaloo.console.app.parser.preset.VariableSetter 变量设置器实现
 * @see xyz.scootaloo.console.app.parser.preset.BackstageTask 将命令提交到后台执行实现
 * @author flutterdash@qq.com
 * @since 2021/2/5 21:10
 */
public interface OptionHandler extends Factory {

    /**
     * 使用一个字符来标识一个操作<br>
     * 注意，这个操作是区分大小写的
     * @return 此参数的内容
     */
    char option();

    /**
     * 带参数运行
     * @param cmd 命令名
     * @param optionParameter 额外的参数
     * @param argItems 命令参数项，已经使用空格分隔成列表
     * @param interpreter 可以执行命令的解释器，由框架注入
     */
    void runWithParameter(String cmd, String optionParameter, List<String> argItems, Interpreter interpreter);

    /**
     * @param cmdName 命令名
     * @param items 命令行参数
     * @return 返回原本输入的命令行
     */
    default String getCompleteCommand(String cmdName, List<String> items) {
        if (!items.isEmpty())
            items.remove(0);
        String args = items.isEmpty() ? "" : String.join(" ", items);
        return cmdName + " " + args;
    }

}
