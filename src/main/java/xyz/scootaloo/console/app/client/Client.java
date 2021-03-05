package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.anno.mark.Private;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.InvokeInfo;
import xyz.scootaloo.console.app.util.BackstageTaskManager.BackstageTaskInfo;
import xyz.scootaloo.console.app.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定义一个连接的用户信息
 * @author flutterdash@qq.com
 * @since 2021/3/2 17:52
 */
@Private
public class Client {
    private static final Console console = ResourceManager.getConsole();
    private final Resources resources;
    protected final String userKey;

    protected Client(String userKey) {
        this.userKey = userKey;
        this.resources = new Resources();
    }

    /**
     * 清空为此用户创建的所有资源
     * @return 一个回调，调用此方法可以实现清空
     */
    public ResourcesHandler shutdown() {
        return new ResourcesHandler(this.userKey, ClientCenter::destroyUserResources);
    }

    public Resources getResources() {
        return resources;
    }

    protected void destroy() {
        resources.shutdown();
    }

    @Override
    public int hashCode() {
        return userKey.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Client))
            return false;
        Client other = (Client) obj;
        return other.userKey.equals(this.userKey);
    }

    @Private
    public static class Resources {
        private Object value;
        private String callingCommand;
        private final History history = new History();
        private final Set<BackstageTaskInfo> taskList = new LinkedHashSet<>();
        private Resources() {
        }

        public String getCallingCommand() {
            return callingCommand;
        }

        public void setCallingCommand(String callingCommand) {
            this.callingCommand = callingCommand;
        }

        public History getHistory() {
            return history;
        }

        public Set<BackstageTaskInfo> getTaskList() {
            return taskList;
        }

        @SuppressWarnings({ "unchecked", "hiding" })
        public <T> T getValue() {
            return (T) value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        private void shutdown() {
            history.hisInfoList.clear();
            taskList.clear();
            callingCommand = "";
        }

    }

    // 实现历史记录功能时使用，前提条件是sys监听器已经启用
    @Private
    public static class History {
//        private final Cursor cursor = new Cursor(this);
        // 历史记录
        private final LinkedList<InvokeInfo> hisInfoList = new LinkedList<>();
        // 日期转换器 执行时间
        private static final SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm");

        // 向容器增加新的命令
        public void add(InvokeInfo info) {
            if (hisInfoList.size() >= ClientCenter.SINGLETON.maxHistory)
                hisInfoList.removeFirst();
            hisInfoList.addLast(info);
        }

        // 筛选出符合条件的记录，并按照规则显示出来
        public List<InvokeInfo> select(String name, int size, boolean isAll, boolean success, boolean rtnVal,
                                       boolean args, boolean invokeAt, boolean interval) {
            boolean matchByName = true;
            if (name == null)
                matchByName = false;
            if (size < 0)
                size = hisInfoList.size();
            else
                size = Math.min(hisInfoList.size(), size);
            LinkedList<InvokeInfo> targetInfos = new LinkedList<>();
            ListIterator<InvokeInfo> it = hisInfoList.listIterator(hisInfoList.size());
            while (it.hasPrevious()) {
                if (size <= 0)
                    break;
                InvokeInfo info = it.previous();
                if (matchByName) {
                    if (info.getName().equals(name)) {
                        targetInfos.addFirst(info);
                        size--;
                    }
                } else {
                    targetInfos.addFirst(info);
                    size--;
                }
            }

            for (InvokeInfo inf : targetInfos) {
                printInfo(inf, isAll, success, rtnVal, args, invokeAt, interval);
            }
            return targetInfos;
        }

        // 显示这些信息
        private static void printInfo(InvokeInfo info, boolean isAll, boolean success,
                                      boolean rtnVal, boolean args,
                                      boolean invokeAt, boolean interval) {
            StringBuilder sb = new StringBuilder();
            // 执行的日期
            if (isAll || invokeAt)
                sb.append('[').append(time_sdf.format(new Date(info.getInvokeAt()))).append("] ");
            // 执行用时
            if (isAll || interval)
                sb.append('[').append(StringUtils.trimNumberSizeTo4(info.getInterval())).append("] ");
            // 命令/方法名
            sb.append("[name: ").append(StringUtils.trimSizeTo7(info.getName())).append("] ");
            // 是否成功
            if (isAll || success)
                sb.append('[').append(info.isSuccess() ? "success" : "failed_").append("] ");
            // 使用的参数
            if (isAll || args)
                sb.append("[args: ").append(String.join(" ", info.getCmdArgs())).append("] ");
            // 返回值
            if (isAll || rtnVal)
                sb.append("[rtn = ").append(getRtnValue(info)).append(']');
            console.println(sb);
        }

        private static String getRtnValue(InvokeInfo info) {
            Object rtn = info.get();
            if (rtn == null)
                return null;
            if (!(rtn instanceof String))
                return rtn.toString();
            String lines = (String) rtn;
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i<lines.length(); i++) {
                char c = lines.charAt(i);
                if (c == '\n')
                    stringBuilder.append("\\n");
                else
                    stringBuilder.append(c);
            }
            return stringBuilder.toString();
        }

        public List<InvokeInfo> getInvokeHistory() {
            return this.hisInfoList;
        }

        protected Cursor getCursor() {
//            return this.cursor;
            return null;
        }

    }

    // 游标
    public static final class Cursor {
        private final History history;
        private ListIterator<InvokeInfo> cursor;

        public Cursor(History history) {
            this.history = history;
            this.cursor = history.getInvokeHistory().listIterator();
        }

        // 获取前一条输入的命令
        public Optional<String> getPre() {
            if (cursor.hasPrevious())
                return Optional.of(getCommand(cursor.previous()));
            return Optional.empty();
        }

        // 获取后一条输入的命令
        public Optional<String> getNext() {
            if (cursor.hasNext())
                return Optional.of(getCommand(cursor.next()));
            return Optional.empty();
        }

        // 更新输入列表
        public void gotoEnd() {
            this.cursor =  history.getInvokeHistory().listIterator();
        }

        private String getCommand(InvokeInfo info) {
            return info.getName() + " " + String.join(" ", info.getCmdArgs());
        }

    }

}
