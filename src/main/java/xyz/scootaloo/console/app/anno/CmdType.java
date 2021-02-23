package xyz.scootaloo.console.app.anno;

/**
 * 命令类型，不同的类型决定了不同的装配方式
 * -------------------------
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:59
 */
public enum CmdType {

    Init,      // 系统初始化时被调用，可以使用 @Cmd 注解的 order() 属性指定执行的优先级
    Filter,    // 命令被调用之前被调用，类似于过滤器
    Destroy,   // 程序销毁后被调用
    Cmd,       // 可被调用的命令
    Parser     // 做为解析器注册到系统

}
