package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.OptionHandle;
import xyz.scootaloo.console.app.util.BackstageTaskManager;

import java.util.List;

/**
 * 后台任务 操作方式处理器
 * 标志是 'D'
 * 例如一个命令行是这样的: sleep 500 代表休眠当前线程500毫秒
 * 而 sleep-Dtest 500 表示提交一个名为"test"的任务到后台，命令行的功能不变，唯一的区别是被转移到了后台。
 * 这个时候你可以使用 tasks 查看后台的任务信息。
 * 使用 task test 查看这个命令的输出.
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
