package xyz.scootaloo.console.app.util;

import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.OutPrinter;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.InvokeInfo;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * 后台任务管理器
 * @author flutterdash@qq.com
 * @since 2021/2/5 21:09
 */
public final class BackstageTaskManager {
    // 线程池 Size : 3
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private static final LinkedHashSet<BackstageTaskInfo> taskList = new LinkedHashSet<>();
    private static final Console console = ResourceManager.getConsole();

    private static PrinterImpl curOutput = PrinterImpl.DFT_PRINTER;

    /**
     * 提交一个任务到后台
     * @param taskName 给后台任务一个名称，用于查找
     * @param callable 后台任务要运行的内容
     */
    public static void submit(String taskName, Callable<InvokeInfo> callable) {
        bind(threadPool.submit(callable), taskName);
    }

    // 防重排序
    private static synchronized void bind(Future<InvokeInfo> future, String taskName) {
        PrinterImpl newOutPrinter = new PrinterImpl(new StringBuffer());
        curOutput = newOutPrinter;
        BackstageTaskInfo needToAdd = new BackstageTaskInfo(taskName, future, newOutPrinter);
        taskList.remove(needToAdd);
        taskList.add(needToAdd);
    }

    //------------------------------Functions----------------------------------

    /**
     * 获取一个输出器，用于使用print或者println这些功能
     * @return 输出器
     */
    public static OutPrinter getPrinter() {
        if (curOutput != null) {
            OutPrinter printer = curOutput;
            curOutput = null;
            return printer;
        }
        return PrinterImpl.DFT_PRINTER;
    }

    // 显示所有任务
    public static void list() {
        StringBuilder stringBuilder = new StringBuilder();
        taskList.forEach(backstageTaskInfo -> backstageTaskInfo.showTask(stringBuilder));
    }

    // isDone 清除已完成的任务或者清除已完成的任务
    public static void clearHistory(boolean isDone) {
        if (isDone) {
            taskList.clear();
            return;
        }
        taskList.removeIf(info -> info.future.isDone());
    }

    /**
     * 显示一个后台任务的输出内容
     * @param taskName 任务名
     * @param size 最后多少条
     */
    public static void showLogs(String taskName, int size) {
        BackstageTaskInfo taskInfo = null;
        for (BackstageTaskInfo info : taskList) {
            if (info.taskName.equalsIgnoreCase(taskName)) {
                taskInfo = info;
                break;
            }
        }
        if (taskInfo == null) {
            console.println("没有找到这个任务: `" + taskName + "`");
            return;
        }
        taskInfo.output.showLog(size);
    }

    //---------------------------------POJO-------------------------------------

    // 存放后台任务的一些信息
    private static class BackstageTaskInfo {
        final String taskName;
        final Future<InvokeInfo> future;
        final PrinterImpl output;
        final long timestamp;

        // constructor
        public BackstageTaskInfo(String taskName, Future<InvokeInfo> future, PrinterImpl output) {
            this.future = future;
            this.taskName = taskName;
            if (output == null)
                throw new RuntimeException("任务提交失败，请重试");
            this.output = output;
            this.timestamp = System.currentTimeMillis();
        }

        // 输出本任务的执行情况
        public void showTask(StringBuilder stringBuilder) {
            stringBuilder.setLength(0);
            boolean isDone = future.isDone();
            if (isDone)
                output.done();
            stringBuilder.append('[').append(taskName).append(']').append(" \t")
                    .append(isDone ? "已完成" : "进行中").append(" ").append("提交于:");
            StringUtils.getHourMinuteSecond(timestamp, stringBuilder).append(" ");
            // 已完成
            if (isDone) {
                final boolean[] hasEx = {false};
                InvokeInfo info = Console.exSupplier(future::get, InvokeInfo::simpleSuccess, (ex) -> {
                    hasEx[0] = true;
                    stringBuilder.append(" error, msg:").append(ex.getMessage());
                });
                if (!hasEx[0]) {
                    if (info.isSuccess()) {
                        stringBuilder.append(" 用时:");
                        StringUtils.getIntervalBySS_MS(info.getInterval(), stringBuilder);
                    } else {
                        stringBuilder.append("- error, msg:").append(info.getExMsg());
                    }
                }
            } else {
                stringBuilder.append(" 已进行:");
                StringUtils.getIntervalBySS_MS(System.currentTimeMillis() - timestamp, stringBuilder);
            }
            console.println(stringBuilder);
        }

        @Override
        public int hashCode() {
            return this.taskName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj instanceof BackstageTaskInfo) {
                BackstageTaskInfo other = (BackstageTaskInfo) obj;
                return other.taskName.equals(this.taskName);
            } else {
                return false;
            }
        }

    }

    // 打印器默认实现
    private static class PrinterImpl implements OutPrinter {
        // 默认实现为直接输出在控制台上
        static final PrinterImpl DFT_PRINTER = new PrinterImpl(new StringBuffer(), true);
        static Consumer<String> outputMode = console::println;

        final StringBuffer sb;
        final LinkedList<String> lines;
        final boolean immediately;

        public PrinterImpl(StringBuffer sb) {
            this(sb, false);
        }

        public PrinterImpl(StringBuffer sb, boolean immediately) {
            this.sb = sb;
            this.sb.setLength(0);
            this.lines = new LinkedList<>();
            this.immediately = immediately;
        }

        // 设置输出的方式，默认调用系统的 System.out.println()
        public void setOutputMode(Consumer<String> zOutputMode) {
            if (zOutputMode != null)
                outputMode = zOutputMode;
        }

        // 显示最后 n 条输出
        public void showLog(int n) {
            if (n < 0)
                lines.forEach(outputMode);
            else {
                int len = Math.min(n, lines.size());
                LinkedList<String> tmp = new LinkedList<>();
                ListIterator<String> listIterator = lines.listIterator(lines.size());
                while (len > 0 && listIterator.hasPrevious()) {
                    String pre = listIterator.previous();
                    tmp.addFirst(pre);
                    len--;
                }
                tmp.forEach(outputMode);
            }
        }

        // 当任务执行完成，将缓存中剩余的内容清除
        public void done() {
            if (immediately)
                return;
            if (sb.length() > 0) {
                lines.add(sb.toString());
                sb.setLength(0);
            }
        }

        @Override
        public void print(Object o) {
            sb.append(o.toString());
        }

        @Override
        public void println(Object o) {
            if (immediately) {
                console.print(o);
                return;
            }
            if (sb.length() > 0) {
                lines.add(sb.toString());
                sb.setLength(0);
            }
            lines.add(o.toString());
        }

        @Override
        public void err(Object o) {
            if (immediately) {
                console.err(o);
            } else {
                lines.add(o.toString());
            }
        }

    }

}
