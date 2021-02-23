package xyz.scootaloo.console.app.common;

/**
 * 控制台的颜色代码，在某些平台可能不生效
 * @see Colorful
 * @author flutterdash@qq.com
 * @since 2020/12/27 16:19
 */
interface ConsoleColor {

    // 白
    String WHITE    = "\u001B[30m";

    // 红
    String RED      = "\u001B[31m";

    // 绿
    String GREEN    = "\u001B[32m";

    // 黄
    String YELLOW   = "\u001B[33m";

    // 蓝
    String BLUE     = "\u001B[34m";

    // 紫
    String PURPLE   = "\u001B[35m";

    // 青
    String CYANOGEN = "\u001B[36m";

    // 灰
    String GRAY     = "\u001B[37m";

}
