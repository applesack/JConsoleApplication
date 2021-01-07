package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.component.*;

import java.util.ArrayList;
import java.util.List;

/**
 *   #####################################
 *  # 提示: 此类可以删除或者修改，不影响系统功能 #
 * ######################################
 *
 * +++++++++++++++++++++++++++++++
 * + 回顾基础功能的使用，请跳转至:     +
 * + {@link QuicklyStart}        +
 * +                             +
 * + 系统中的插件开发示例，请跳转至:   +
 * + {@link PluginDemo}         +
 * +                            +
 * + 简易的登陆系统实现示例，请跳转至: +
 * + {@link LoginDemo}          +
 * ++++++++++++++++++++++++++++++
 *
 * ---- 进阶用法 ----
 * + 命令工厂的生命周期
 * + 命令的类型
 * + 自定义的过滤器
 * + 可选参数和必选参数
 * ----------------
 * @author flutterdash@qq.com
 * @since 2020/12/28 12:57
 */
public class AdvancedDemo {

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
     * [可选参数 & 必选参数]
     * - @Opt 可选参数，@Opt注解的参数是一个字符，这个字符代表参数的名称，这个字符与注解的参数没有关系，只是做一个标识，
     *          有三种模式可以使用可选参数，
     *          1. 例如 -abc 这种格式代表选中了a、b和c三个选项，-ab 则表示选中了a和b两个选项。
     *              选中的选项对应的方法参数就是true，否则就是false，这种模式只针对boolean类型的参数的简化写法，对于其他类型不生效。
     *          2. 例如 -a -b -c 分开写，效果等同于 -abc
     *          3. 例如 -a=true -b=false -c=11，使用这种模式会把等于号后面的数值赋值给方法参数。
     *          三种方式可以混合使用，并且位置不受影响，
     *              -a -b  等于 -b -a
     *              -ab    等于 -ba
     *              -a=1 -b=2 等于 -b=2 -a=1
     *          注意： 可选参数 @Opt 注解可以用 defVal 参数指定一个默认值，当该选项没有被选中时默认值生效
     * - @Req 必选参数，@Req注解的参数是必选项的标记，以当前这个方法为例，--name=小明，则小明这个属性被注入到了name参数中
     *          注意：当缺少必选参数，方法无法被调用
     *
     * ----------------测试命令----------------
     *       opt -ab -c=13 --name=xiaoMing
     *       opt --name=xiaoMing -a
     *       opt --name=xiaoMing
     * --------------------------------------
     * @param a -
     * @param b -
     * @param c -
     * @param name -
     */
    @Cmd
    public void opt(@Opt('a') boolean a, @Opt('b') boolean b, @Opt(value = 'c', defVal = "19") int c,
                    @Req("name") String name) {

        List<Character> options = new ArrayList<>();
        if (a) options.add('a');
        if (b) options.add('b');
        System.out.println(options);
        System.out.println("c: " + c);
        System.out.println("你好: " + name);
    }

    /**
     * 有了这两个功能以后，下面写一个方法来模拟Docker的run的命令
     *
     * ----------------测试命令----------------
     * run bootshiro --name=bootshiro
     * run --name=bootshiro -dit -v=/data:/home/usr -p=8080:80 bootshiro
     * run --name=bootshiro -it -v=/data:/home/usr -p=8080:80 bootshiro shell
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
                     @Req("name") String name,
                     @Opt('v') String pathMapping, @Opt('p') String portMapping,
                     @Opt(value = '*', defVal = "/bin/bash") String interpreter) {

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
     * @return 最终类型，于targets一致，起码是能相互转换的
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
