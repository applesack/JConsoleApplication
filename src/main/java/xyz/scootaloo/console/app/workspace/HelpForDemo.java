package xyz.scootaloo.console.app.workspace;

/**
 * 为命令增加帮助信息
 *
 * 通常对于自己写的命令不太需要提示，
 * 假如写好的东西要供别人使用，那么帮助信息也许是必要的
 *
 * 框架用于加载帮助信息使用了比较简单的实现方式
 * 可以专门新建一个类来管理命令的帮助信息，就像这个类一样
 *
 * 这个类中的无参、且返回值是string类型的方法就用于提供帮助信息
 * 为了和已有的类方法进行区别，可以在方法名前面或者后面加上下划线
 *
 * 以存在的命令帮助，在控制台中可以用
 *      help [命令名]
 * 查看
 * @author flutterdash@qq.com
 * @since 2021/1/12 10:27
 */
public class HelpForDemo {
    // 推荐将帮助信息类做成单例
    public static final HelpForDemo INSTANCE = new HelpForDemo();

    // 对于 QuickStart 的帮助信息

    private String _hello() {
        return "hello world\n" +
                "接收一个参数，将在输出 hello [name] 的结果\n" +
                "示例:\n" +
                "       hello java\n";
    }

    private String _add() {
        return "计算两个数的和\n" +
                "接收两个参数，且两个参数都是整数\n" +
                "示例: \n" +
                "       add 12 14\n" +
                "       add 100 200\n";
    }

    private String _sub() {
        return "计算两个数相减的商\n" +
                "接收两个参数，且两个参数都是整数\n" +
                "示例: \n" +
                "       sub 11 10\n" +
                "       sub 10 13\n";
    }

    private String _mul() {
        return "计算两个数相乘的积\n" +
                "接收两个参数，且两个参数都是整数\n" +
                "示例:\n" +
                "       mul 10 10\n" +
                "       mul 256 256\n";
    }

    private String _g() {
        return "演示集合的功能\n" +
                "此命令有3个参数，第一个参数将被转换成整型数组，第二个参数将被转换成单精度浮点的列表，\n" +
                "   第三个参数将被转换成字符串的集，每个参数的各个数据项之间用英文的逗号分隔.\n" +
                "示例:\n" +
                "   g 1,2,3,1 1,2,1.1,2 1,1,1,2,2\n";
    }

    private String _avg() {
        return "计算3个数的平均值\n" +
                "接收3个参数，第一个参数是整数，第二个参数是单精度浮点，第三个参数是双精度浮点\n" +
                "示例:\n" +
                "       avg 12 12.3 12.1\n";
    }

    private String _addStu() {
        return "测试表单输入的功能:\n" +
                "假如参数中含有表单类，那么可以由系统自带的表单功能完成参数补全\n" +
                "在对应参数的位置需要一个占位符，其他的交给系统就行\n" +
                "示例:\n" +
                "       addStu #\n" +
                "接下来就可以看到自动提示的输入了，输入完成后用Y或者N确认是否需要继续修改\n" +
                "提示: 表单类是类上有 @Form 注解的类，类属性上含有 @Prop 注解则可以被系统处理\n";
    }

    // 对于 AdvancedDemo 的帮助信息



}
