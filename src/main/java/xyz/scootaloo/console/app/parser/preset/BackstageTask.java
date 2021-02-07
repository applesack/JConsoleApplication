package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.OptionHandle;
import xyz.scootaloo.console.app.util.BackstageTaskManager;

import java.util.List;

/**
 * 后台任务 操作方式处理器
 * @author flutterdash@qq.com
 * @since 2021/2/5 21:13
 */
public final class BackstageTask implements OptionHandle {
    // 单例
    protected static final BackstageTask INSTANCE = new BackstageTask();

    @Override
    public char option() {
        return 'D';
    }

    @Override
    public void runWithParameter(String cmd, String optionParameter,
                                 List<String> argItems, Interpreter interpreter) {
        String completeCmd = getCompleteCommand(cmd, argItems);
        BackstageTaskManager.submit(optionParameter, () -> interpreter.interpret(completeCmd));
    }

}
