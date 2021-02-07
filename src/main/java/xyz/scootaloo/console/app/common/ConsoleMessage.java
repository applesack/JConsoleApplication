package xyz.scootaloo.console.app.common;

import xyz.scootaloo.console.app.listener.EventPublisher;

/**
 * 控制台消息
 * 本框架不处理控制台消息，
 * 但是可以使用事件监听器来监听被提交的消息
 * @author flutterdash@qq.com
 * @since 2021/2/5 15:31
 */
public class ConsoleMessage {
    // 定义了控制台消息的4个级别
    enum Type {
        NORMAL,  // 一般
        MESSAGE, // 消息
        WARNING, // 警告
        ERROR    // 错误
    }

    private Type type;           // 消息类型
    private String msgTitle;     // 消息的标题
    private String msgContent;   // 消息的内容
    private long timestamp;      // 产生此消息的日期
    private Throwable throwable; // 此消息携带的异常信息 (假如有)

    // 提交消息
    public static void submitMsg(String title, String content) {
        submit(get(Type.MESSAGE, title, content, null));
    }

    // 提交信息
    public static void submitInfo(String title, String content) {
        submit(get(Type.NORMAL, title, content, null));
    }

    // 提交警告
    public static void submitWarning(String msgTitle, String msgContent) {
        submit(get(Type.WARNING, msgTitle, msgContent,null));
    }

    // 提交错误
    public static void submitError(String msgTitle, String msgContent, Throwable throwable) {
        submit(get(Type.ERROR, msgTitle, msgContent, throwable));
    }

    private static ConsoleMessage get(Type type, String msgTitle, String msgContent, Throwable throwable) {
        ConsoleMessage message = new ConsoleMessage();
        message.type = type;
        message.timestamp = System.currentTimeMillis();
        message.msgTitle = msgTitle;
        message.msgContent = msgContent;
        message.throwable = throwable;
        return message;
    }

    private static void submit(ConsoleMessage message) {
        EventPublisher.onMessage(message);
    }

    private ConsoleMessage() {
    }

}
