package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.component.Cmd;
import xyz.scootaloo.console.app.support.component.Form;
import xyz.scootaloo.console.app.support.component.Prop;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *   #####################################
 *  # 提示: 此类可以删除或者修改，不影响系统功能 #
 * ######################################
 *
 * ---- 快速开始，演示基本功能 ----
 * 方法即是命令，方法参数即是命令参数
 *
 * 系统的进阶用法，请跳转至:
 * {@link AdvancedDemo}
 *
 * @author flutterdash@qq.com
 * @since 2020/12/30 20:32
 */
public class QuickStart {

    /**
     * 尝试在控制台输入 hello world
     * 然后`world`被做为了hello()方法的参数，结果输出
     * @param name 这个方法只有一个参数，所以输入命令时只能带一个参数，尝试以下输入
     *             hello
     *             hello 1 2 3
     *             hello java
     * 提示：方法的访问属性不做要求，public 或者 private 都行
     */
    @Cmd // 含有 @Cmd 注解的方法会被系统处理，并做为命令注册到系统中
    public void hello(String name) {
        System.out.println("hello " + name);
    }

    /**
     * 与上面的hello命令相似，这个命令将输入两个整型数值的和，尝试输入：
     *              add 1 2
     *              add 1
     *              add null a
     * 当参数不能由字符串转换成方法的参数类型时，会抛出异常，但是异常只在开发过程中具有意义,
     * 如果看不到控制台的调用栈，请在启动类中进行设置
     * @param a -
     * @param b -
     */
    @Cmd
    private int add(int a, int b) {
        System.out.println(a + b);
        return a + b;
    }

    /**
     * - @Cmd 注解中的参数 name 代表别名，你可以用sub命令调用这个方法，也可以用subtract命令来调用，尝试输入:
     *              sub 10 5
     *              subtract 10 5
     * @param a -
     * @param b -
     */
    @Cmd(name = "subtract")
    private void sub(int a, int b) {
        System.out.println(a - b);
    }

    // 注意： 方法中有多个参数时，请用空格将各个参数隔开，例如
    //              mul 12 13
    @Cmd(name = "multiply")
    private void mul(int a, Integer b) {
        System.out.println(a * b);
    }

    /**
     * 方法参数中可以使用集合，对于数组，建议使用基本类型的包装类，否则会引发异常
     * 尝试输入
     *      g 1,2,4,3 1.1,1.2,1.3 11,12,11,13
     * 所有数值都被正确处理了，这里参数之间还是使用空格隔开，用逗号分割集合参数的各个数据项
     * 目前支持 数组array 列表List 集Set
     * List 实现是 ArrayList
     * Set  实现是 LinkedHashSet
     * : 后续可能会增加对Queue和Map的支持
     * @param arr -
     * @param floats -
     * @param set -
     */
    @Cmd
    private void g(Integer[] arr, List<Float> floats, Set<String> set) {
        System.out.println(Arrays.toString(arr));
        System.out.println(floats);
        System.out.println(set);
    }

    /**
     * 系统会按照参数的类型进行数值转换，对于数值类型不会丢失原本的精度，即使是包装类也不受影响
     * 尝试输入:
     *              avg 10 10.1 10.2
     *              avg 10 10 10
     * @param a 整型
     * @param b 单精度浮点
     * @param c 双精度浮点
     */
    @Cmd(name = "average")
    public void avg(int a, Float b, Double c) {
        double sum = 0;
        sum += a;
        sum += b;
        sum += c;
        sum /= 3;
        System.out.println(sum);
    }

    /**
     * 表单功能
     * 尝试输入
     *              stuAdd #
     * 提示: 命令参数中表单的位置用任意占位符代替
     * @param student 由键盘输入生成的对象
     */
    @Cmd(name = "stuAdd")
    private void addStudent(Student student) {
        System.out.println("姓名： " + student.name);
        System.out.println("年龄： " + student.age);
    }

    /**
     * 表单类的使用，
     * 需要在类上增加 @Form 注解，
     * 另外需要由键盘输入值的属性上增加 @Prop 注解
     * 然后这个类就可以做为 命令方法 的参数了
     */
    @Form(dftExtCmd = "-") // 这个注解参数表示，当输入了这个 "-" 符号，则执行退出继续输入，
    public static class Student {
        // prompt属性表示输入此项数据时的提示，isRequired表示此属性是必选项，未获得有效数据的时候无法退出
        @Prop(prompt = "输入学生姓名", isRequired = true)
        private String name;

        // @Prop 注解默认 isRequired=false 可以跳过，或者输入退出标志退出输入
        @Prop(prompt = "输入学生年龄")
        private int age;

        // 表单类需要提供一个无参构造方法，private 或者 public 无所谓
        public Student() {
        }
    }

}
