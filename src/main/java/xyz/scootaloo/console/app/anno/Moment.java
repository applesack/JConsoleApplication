package xyz.scootaloo.console.app.anno;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/30 9:32
 */
public enum Moment {

    // 系统启动之初，完成命令装配，但是还未执行 Init 方法时被调用
    OnAppStarted,

    // 系统接收到键盘输入的命令时被调用
    OnInput,

    // 获取输入之后，解析之前
    OnResolveInput,

    // 解析之后
    OnInputResolved

}
