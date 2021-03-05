package xyz.scootaloo.console.app.parser;

import java.util.List;

/**
 * 命令行执行器 <br>
 * <p>框架中对外部暴露出来的可调用接口，每个 {@code Actuator} 对象代表一个可被调用的命令。<br>
 * 其中方法参数 {@code cmdArgs} 是命令行需要的参数项，控制台输入的命令行实际上是提取的命令行参数后放置到 {@code cmdArgs} 上
 * 并调用了这个方法，来实现调用命令行即调用java方法的效果。</p>
 * <pre>{@code
 * 对应关系
 *          一个执行器       一个参数解析器        一个Java方法
 *          Actuator --> ParameterParser --> methodObject
 * }</pre>
 * @see NameableParameterParser 参数解析器接口
 * @see xyz.scootaloo.console.app.parser.Interpreter.MethodActuator 框架中此接口的实现
 * @author flutterdash@qq.com
 * @since 2020/12/27 22:34
 */
@FunctionalInterface
public interface Actuator {

    /**
     * 接收字符串列表做为参数，进行执行
     * @param cmdArgs 命令行参数，已经使用空格分隔成了列表
     * @return 命令执行过程中收集到的信息，具体请查看 {@link InvokeInfo}
     */
    InvokeInfo invoke(List<String> cmdArgs);

}
