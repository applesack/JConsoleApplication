package xyz.scootaloo.console.app.parser;

/**
 * 给参数解析器起个名字
 * <p>要实现自定义的解析器，必须实现这个接口。
 * 假设你已经创建了一个类实现了这个接口，并且在配置中注册了实现类的实例，比如这样:</p>
 * <pre>{@code ApplicationRunner.consoleApplication(
 *                 Console.factories()
 *                         .add(new YourImpl(), true)
 *                         .ok()).run();
 * }</pre>
 * <p>这里假设 {@code YourImpl} 的 {@code name()} 方法返回的是 “myImpl”, 那么使用时:</p>
 * <pre>{@code @Cmd(parser = "myImpl")
 * public void test(int a, int b) {
 *     System.out.println(a + b);
 * }}</pre>
 * <p>这样，当你在控制台输入 test 这个命令的时候，YourImpl的解析参数方法就会被调用</p>
 * @see xyz.scootaloo.console.app.parser.preset.SubParameterParser 一个系统中预置的解析器实现供参考
 * @author flutterdash@qq.com
 * @since 2021/1/18 11:44
 */
public interface NameableParameterParser extends ParameterParser {

    /**
     * 解析器的名称，这样可用按照字符串来找到对应的解析器
     * @return 解析器的名称
     */
    String name();

}
