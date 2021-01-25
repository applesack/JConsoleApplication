package xyz.scootaloo.console.app.anno;

import xyz.scootaloo.console.app.listener.Moment;

/**
 * 命令类型，
 * @see Moment
 * -------------------------
 * @author flutterdash@qq.com
 * @since 2020/12/28 13:59
 */
public enum CmdType {

    Init,      // 系统初始化时
    Pre,       // 命令被调用之前
    Destroy,   // 程序销毁后
    Cmd,       // 可被调用的命令
    Parser     // 做为解析器注册到系统

}
