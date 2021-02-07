package xyz.scootaloo.console.app.parser;

import java.util.List;

/**
 * 额外参数的处理器
 * @author flutterdash@qq.com
 * @since 2021/2/5 21:10
 */
public interface OptionHandle {

    /**
     * 使用一个字符来标识一个操作
     * 注意，这个操作是区分大小写的
     * @return 此参数的内容
     */
    char option();

    /**
     * 带参数运行
     * @param cmd 命令名
     * @param optionParameter 额外的参数
     * @param argItems 命令参数项，已经使用空格分隔成列表
     * @param interpreter 可以执行命令的解释器
     */
    void runWithParameter(String cmd, String optionParameter, List<String> argItems, Interpreter interpreter);

    default String getCompleteCommand(String cmdName, List<String> items) {
        if (!items.isEmpty())
            items.remove(0);
        String args = items.isEmpty() ? "" : String.join(" ", items);
        return cmdName + " " + args;
    }

}
