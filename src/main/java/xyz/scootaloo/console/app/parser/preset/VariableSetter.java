package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.InvokeInfo;
import xyz.scootaloo.console.app.parser.OptionHandler;
import xyz.scootaloo.console.app.support.VariableManager;

/**
 * 变量设置器
 * @author flutterdash@qq.com
 * @since 2021/2/7 9:40
 */
@Stateless
public final class VariableSetter implements OptionHandler {
    /** singleton */
    protected static final VariableSetter INSTANCE = new VariableSetter();
    private static final Console console = ResourceManager.getConsole();

    @Override
    public char option() {
        return 'V';
    }

    @Override
    public void runWithParameter(String cmd, String optionParameter, String argItems, Interpreter interpreter) {
        String completeCmd = getCompleteCommand(cmd, argItems);
        InvokeInfo info = interpreter.interpret(completeCmd);
        if (info.isSuccess()) {
            VariableManager.set(optionParameter, info.get(),
                    Interpreter.getCurrentUser().getResources().getVariablePool());
        } else {
            console.println("方法执行错误");
        }
    }

    @Override
    public String toString() {
        return getOptionString();
    }
}
