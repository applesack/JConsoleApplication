package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.anno.mark.Private;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.Interpreter.FilterChainMessage;
import xyz.scootaloo.console.app.parser.InvokeInfo;
import xyz.scootaloo.console.app.util.BackstageTaskManager.BackstageTaskInfo;
import xyz.scootaloo.console.app.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 定义一个连接的用户信息
 *
 * @author flutterdash@qq.com
 * @since 2021/3/2 17:52
 */
@Private
public class Client {
    private static final Console console = ResourceManager.getConsole();
    /** 每个连接进来的用户都会分配一个资源对象 */
    private final Resources resources;
    /** 此用户的标识 */
    protected final String userKey;

    protected Client(String userKey) {
        this.userKey = userKey;
        this.resources = new Resources();
    }

    /**
     * 清空为此用户创建的所有资源
     * @return 一个回调，调用回调方法可以实现清空资源
     */
    public ResourcesHandler shutdown() {
        return new ResourcesHandler(this.userKey, ClientCenter::destroyUserResources);
    }

    /**
     * @return 获取这个资源对象
     */
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

    @Override
    public String toString() {
        return "Client{" +
                "resources=" + resources +
                ", userKey='" + userKey + '\'' +
                '}';
    }

    @Private
    public static class Resources {
        private Object value; // 一个属性
        private String callingCommand; // 当前正在处理的命令行
        private final History history = new History(); // 执行命令行的信息记录
        private final Set<BackstageTaskInfo> taskList = new LinkedHashSet<>(); // 后台任务列表
        private final Map<String, Object> variablePool = new HashMap<>(); // 变量池，存储一些键值对
        private final ReplacementRecord replacementRecord = new ReplacementRecord(); // 命令行中占位符替换记录
        private final FilterChainMessage filterChainMessage
                = new FilterChainMessage(); // 过滤链执行信息

        private Resources() {
        }

        /**
         * @return 获取当前用户的过滤链执行情况
         */
        public FilterChainMessage getFilterChainMessage() {
            return this.filterChainMessage;
        }

        /**
         * @return 获取当前正在处理的命令行
         */
        public String getCallingCommand() {
            return callingCommand;
        }

        /**
         * @param callingCommand 设置当前正在处理的命令行
         */
        public void setCallingCommand(String callingCommand) {
            this.callingCommand = callingCommand;
        }

        /**
         * @return 获取当前用户执行的命令行的历史记录
         */
        public History getHistory() {
            return history;
        }

        /**
         * @return 获取当前用户提交的后台任务信息列表
         */
        public Set<BackstageTaskInfo> getTaskList() {
            return taskList;
        }

        /**
         * @return 命令行中占位符替换记录
         */
        public ReplacementRecord getReplacementRecord() {
            return replacementRecord;
        }

        /**
         * @return 获取当前用户的变量池
         */
        public Map<String, Object> getVariablePool() {
            return this.variablePool;
        }

        /**
         * 获取当前用户之前存储的对象
         * @param <T> 类型
         * @return 拿到这个对象
         */
        @SuppressWarnings({ "unchecked", "hiding" })
        public <T> T getValue() {
            if (value == null)
                return null;
            return (T) value;
        }

        /**
         * @param value 给当前这个用户设置一个对象，类似于 session 功能
         */
        public void setValue(Object value) {
            this.value = value;
        }

        private void shutdown() {
            history.hisInfoList.clear();
            taskList.clear();
            variablePool.clear();
            replacementRecord.refresh();
            callingCommand = "";
        }

        @Override
        public String toString() {
            return "Resources{" +
                    "value=" + value +
                    ", callingCommand='" + callingCommand + '\'' +
                    ", history=" + history +
                    ", taskList=" + taskList +
                    ", variablePool=" + variablePool +
                    ", replacementRecord=" + replacementRecord +
                    ", filterChainMessage=" + filterChainMessage +
                    '}';
        }

    }

    // 实现历史记录功能时使用，前提条件是sys监听器已经启用
    @Private
    public static class History {
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

        @Override
        public String toString() {
            return "History{" +
                    "historySize=" + hisInfoList.size() +
                    '}';
        }

    }

}
