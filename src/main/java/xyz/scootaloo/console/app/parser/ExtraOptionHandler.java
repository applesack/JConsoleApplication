package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理在命令名上插入的额外参数 <br>
 * 此功能只在以ConsoleApplication的方式启动时有效
 * @author flutterdash@qq.com
 * @since 2021/2/5 21:30
 */
public final class ExtraOptionHandler {
    private static final Map<Character, OptionHandler> optionMap = new HashMap<>();
    private static final Console console = ResourceManager.getConsole();
    private static final String DELIMITER = "-";
    private static Interpreter interpreter;

    /**
     * 一个比较容易想到的办法就是通过 setter 将解释器注入进来，缺点是暴露了一个没什么用的API
     * @param zInterpreter -
     */
    protected static void setInterpreter(Interpreter zInterpreter) {
        if (zInterpreter != null)
            interpreter = zInterpreter;
    }

    /**
     * 从工厂中注入进来的处理器
     * @param handle 实现 OptionHandle 接口
     */
    protected static void addExtraOption(OptionHandler handle) {
        optionMap.put(handle.option(), handle);
    }

    /**
     * 对于一条命令行，做如下解析
     *      命令名，命令行中最前的一个单词，假如它由‘-’分隔，且后面的内容不为空，则被认为是有效的
     *      然后查找对应的处理器
     * @param cmdName 命令名
     * @param args 参数
     * @return 是否被处理过
     */
    protected static boolean handle(String cmdName, List<String> args) {
        if (!cmdName.contains(DELIMITER))
            return false;
        String[] segments = cmdName.split(DELIMITER);
        if (segments.length != 2 || segments[0].length() == 0 || segments[1].length() == 0)
            return false;
        String realCmdName = segments[0];
        char option = segments[1].charAt(0);
        if (optionMap.containsKey(option)) {
            if (interpreter == null) {
                console.println("未找到解释器");
                return false;
            }
            OptionHandler handle = optionMap.get(option);
            handle.runWithParameter(realCmdName, getParameter(segments[1]), args, interpreter);
        } else {
            console.println("没有找到对应的操作处理器: `" + option + "`");
        }
        return true;
    }

    private static String getParameter(String info) {
        if (info.length() == 0)
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i<info.length(); i++)
            stringBuilder.append(info.charAt(i));
        return stringBuilder.toString();
    }

}
