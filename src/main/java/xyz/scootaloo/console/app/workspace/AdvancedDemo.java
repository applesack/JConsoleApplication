package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.component.*;

import java.util.ArrayList;
import java.util.List;

/**
 *   #####################################
 *  # 提示: 此类可以删除或者修改，不影响系统功能 #
 * ######################################
 *
 * ++++++++++++++++++++++++++++++
 * + 回顾基础功能的使用，请跳转至:    +
 * + {@link QuickStart}         +
 * +                            +
 * + 系统中的监听器开发，请跳转至:    +
 * + {@link ListenerDemo}       +
 * +                            +
 * + 简易的登陆系统实现示例，请跳转至: +
 * + {@link LoginDemo}          +
 * ++++++++++++++++++++++++++++++
 *
 * ---- 进阶用法 ----
 * + 命令工厂的生命周期
 * + 命令的类型
 * + 自定义的过滤器
 * + 可命名参数的使用
 * ----------------
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
public class AdvancedDemo {

    /**
     * 2020/1/16 更新
     * 默认是由系统来实现参数解析的过程，但是你也可以选择自己来处理参数
     * 将 parser 标记为raw ，这是在设置中注入的一个解析逻辑实现，什么都不做
     * 尝试输入 raw Far More Great and Powerful
     * 参数原封不动地传递进来了，但是要求方法参数必须只有一个且是String
     *
     * 可以自己实现一个ParameterParser并注入到设置中，标记parser来选择参数解析器
     * @param line -
     */
    @Cmd(parser = "raw")
    private void raw(String line) {
        System.out.println("\"" + line + "\"");
    }

    /**
     * [初始化方法] type = CmdType.Init
     * 在 @Cmd 注解中修改type属性为 CmdType.Init ， 则这个方法会在系统装配完成所有命令后被调用。
     * 注意：
     *      - 标记为 Init 的命令不能含有参数
     *      - 当初始化方法中抛出了异常，则系统启动失败
     *      - 可以使用order注解参数决定方法的调用顺序，数字越小越优先，默认数值是5
     *      - 虽然Init方法有 @Cmd 注解，但是不能做为命令来被调用
     */
    @Cmd(type = CmdType.Init, order = 1)
    private void init1() {
        System.out.println("系统启动完成 优先级 1");
    }

    // 参见上一条，因为这里的order值是-1，所有会先被调用
    @Cmd(type = CmdType.Init, order = -1)
    private void init2() {
        System.out.println("系统启动完成 优先级 -1");
    }

    /**
     * [销毁时方法] type = CmdType.Destroy
     * 规则和Init一样，都不能有参数，返回值无所谓，跟其他Cmd方法不一样，没有地方接收这种返回值
     * 标记为 CmdType.Destroy 的方法，会在系统关闭时被调用，回收资源的代码可以写在这里，比如在程序退出时检查是否需要保存文本。
     */
    @Cmd(type = CmdType.Destroy)
    private void onExit() {
        System.out.println("程序被关闭");
    }

    /**
     * [过滤器方法] type = CmdType.Pre
     * 标记为Pre的方法，会在普通Cmd方法之前被调用，同时过滤器方法必须有一个boolean类型的返回值，
     * 当这个方法返回true，则轮到下一个过滤器处理，假如没有下一个过滤器，再才执行当前的命令
     * 提示:
     *      - 这样的多个过滤方法可以组成一个过滤链，过滤链中任意一环返回false则过滤链中断，当前命令不被执行
     *      - 可以通过order参数指定过滤链的顺序
     *      - 当过滤链返回false时，可以通过onError参数在控制台输出过滤失败的原因
     * @return 是否放行
     */
    @Cmd(type = CmdType.Pre, order = 1, onError = "过滤器没有放行的原因")
    public boolean filter1() {
        System.out.println("经过第一个过滤器");
        return true; // 尝试将true修改为false，重新运行查看命令调用情况
    }

    // 同上一条
    @Cmd(type = CmdType.Pre, order = 2)
    public boolean filter2() {
        System.out.println("经过第二个过滤器");
        return true;
    }

    /**
     * [可命名参数]
     * - @Opt 这个注解可以用于将某个方法参数以类似linux命令的方式调用，这种方式的参数以一个横杠紧接着一个参数名，然后在空格后是参数值
     *          例如这样的 opt -name xiaoMing
     *        然后这个注解有几个属性，
     *        value 此参数的简称
     *        fullName 用一个参数的全称来指向这个方法参数
     *        required 表示这个方法是否是必须的，假如是，命令中没有这个参数则方法不会被调用。默认是false
     *        dftVal 在命令中没有参数时，提供一个默认值
     * 注意: value 和 fullName 效果相同，只是习惯上单横杠接参数简称，双横杠接参数全称，比如 -n jack 等于 --name jack
     * 注意: 在命名参数中，假如对应的方法参数是布尔值，则对应的命令值可以省略，例如 -a true 等于 -a
     *      多个布尔类型的命令参数，可以写到一起，例如 -a -b -c 等于 -abc 等于 -ab -c
     * 注意: 参数默认值的使用，@Opt的defVal属性虽然是字符串，但系统会自动将字符串转换成对应的基础类型，请放心使用
     * ----------------测试命令----------------
     *       opt -ab -c --name jack
     *       opt -n jack -a -d 15
     *       opt --name jack
     *       opt -n jack -abc -d 12 70
     * --------------------------------------
     * @param a -
     * @param b -
     * @param c -
     * @param name -
     */
    @Cmd
    public void opt(@Opt('a') boolean a, @Opt('b') boolean b, @Opt('c') boolean c,
                    @Opt(value = 'd', dftVal = "19") int d,
                    @Opt(value = 'n', fullName = "name", required = true) String name, @Opt('g') int age) {

        List<Character> options = new ArrayList<>();
        if (a) options.add('a');
        if (b) options.add('b');
        if (c) options.add('c');
        System.out.println(options);
        System.out.println("d: " + d);
        System.out.println("age: " + age);
        System.out.println("你好: " + name);
    }

    /**
     * 有了这两个功能以后，下面写一个方法来模拟Docker的run的命令
     *
     * ----------------测试命令----------------
     * run bootshiro --name bootshiro
     * run --name bootshiro -dit -v /data:/home/usr -p 8080:80 bootshiro
     * run --name bootshiro -it -v /data:/home/usr -p 8080:80 bootshiro shell
     * --------------------------------------
     *
     * 2020/12/30 补充一个新特性
     *      当 @Opt 的参数是通配符 '*' 的时候
     *      会将命令行中多余的参数映射到这个通配符上，这就是为什么第三个命令行的shell能够映射到interpreter参数上了。
     *      标记为 '*' 的通配符参数，放在所有的参数末尾才能生效，有多个通配符参数时按照顺序填充
     *
     * @param imageName -
     * @param d -
     * @param i -
     * @param t -
     * @param name -
     * @param pathMapping -
     * @param portMapping -
     */
    @Cmd
    private void run(String imageName,
                     @Opt('d') boolean d, @Opt('i') boolean i, @Opt('t') boolean t,
                     @Opt(value = 'n', fullName = "name") String name,
                     @Opt('v') String pathMapping, @Opt('p') String portMapping,
                     @Opt(value = '*', dftVal = "/bin/bash") String interpreter) {

        List<Character> options = new ArrayList<>();
        if (d) options.add('d');
        if (i) options.add('i');
        if (t) options.add('t');

        System.out.println("镜像名称 => " + imageName);
        System.out.println("参数    => " + options);
        System.out.println("容器名称 => " + name);
        System.out.println("路径映射 => " + pathMapping);
        System.out.println("端口映射 => " + portMapping);
        System.out.println("解释器  => " + interpreter);
    }

    /**
     * 自定义某种类型的处理方式，这里示例对byte类型的自定义处理方式，将byte结果进行自增1
     * 注意，方法参数是String，这个是不能变的
     * @param bt String，且只能有这一个参数
     * @return 最终类型，于targets一致，起码是返回值能向targets转换的
     */
    @Cmd(type = CmdType.Parser, targets = {byte.class, Byte.class})
    public byte resolveByte(String bt) {
        byte b = Byte.parseByte(bt);
        return ++b;
    }

    /**
     * 测试上面的功能，尝试输入
     *      tb 127
     *      tb 128
     * @param b 键盘输入的一个byte值
     */
    @Cmd(name = "tb")
    public void testByte(Byte b) {
        System.out.println(b);
    }

}
