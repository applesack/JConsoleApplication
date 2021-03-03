package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.anno.mark.Private;
import xyz.scootaloo.console.app.common.Console;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.parser.Interpreter;
import xyz.scootaloo.console.app.parser.InvokeInfo;
import xyz.scootaloo.console.app.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 12:14
 */
public class ClientCenter {
    private final Map<String, User> users;
    private static final Console console = ResourceManager.getConsole();
    private final Interpreter interpreter;
    private final int maxHistory;

    private static volatile ClientCenter SINGLETON;

    private ClientCenter(Interpreter interpreter) {
        this.users = new ConcurrentHashMap<>();
        this.interpreter = interpreter;
        this.maxHistory = interpreter.getConsoleConfig().getMaxHistory();
    }

    public static ClientCenter getInstance(Interpreter interpreter) {
        if (SINGLETON == null) {
            synchronized (ClientCenter.class) {
                if (SINGLETON == null) {
                    SINGLETON = new ClientCenter(interpreter);
                }
            }
        }
        return SINGLETON;
    }

    public static void show() {
        System.out.println();
    }

    /**
     * 根据用户标识创建一个用户对象，假如此用户标识已经存在，则拿到已存在的用户对象，否则拿到新创建出来的对象
     * @param userKey 用户标识，必须是唯一的
     * @return 用户对象，可使用此对象管理资源
     */
    public User createUser(String userKey) {
        User user, existUser;
        synchronized (SINGLETON.users) {
            user = new User(userKey);
            existUser = SINGLETON.users.putIfAbsent(userKey, user);
            if (existUser != null)
                user = existUser;
        }
        return user;
    }

    /**
     * 定义一个连接的用户信息
     * @author flutterdash@qq.com
     * @since 2021/3/2 17:52
     */
    public static class User {
        private final String userKey;
        private final Resources resources;

        public User(String userKey) {
            this.userKey = userKey;
            this.resources = new Resources(this);
        }

        /**
         * 清空为此用户创建的所有资源
         * @return 一个回调，调用此方法可以实现清空
         */
        public DestroyResources shutdown() {
            return () -> {
                synchronized (SINGLETON.users) {
                    Optional.ofNullable(SINGLETON.users.remove(userKey))
                            .ifPresent(User::destroy);
                }
            };
        }

        public Resources getResources() {
            return resources;
        }

        public String getUserKey() {
            return userKey;
        }

        private void destroy() {
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
            if (!(obj instanceof User))
                return false;
            User other = (User) obj;
            return other.userKey.equals(this.userKey);
        }

    }

    public static class Resources {
        private final User user;
        private String callingCommand;
        private History history = new History();
        private Resources(User user) {
            this.user = user;
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

        private void shutdown() {
            history.history.clear();
            callingCommand = null;
        }

    }

    // 实现历史记录功能时使用，前提条件是sys监听器已经启用
    @Private
    public static class History {
//        private final Cursor cursor = new Cursor(this);
        // 历史记录
        private final LinkedList<InvokeInfo> history = new LinkedList<>();
        // 日期转换器 执行时间
        private static final SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm");

        // 向容器增加新的命令
        public void add(InvokeInfo info) {
            if (history.size() >= SINGLETON.maxHistory)
                history.removeFirst();
            history.addLast(info);
        }

        // 筛选出符合条件的记录，并按照规则显示出来
        public List<InvokeInfo> select(String name, int size, boolean isAll, boolean success, boolean rtnVal,
                                               boolean args, boolean invokeAt, boolean interval) {
            boolean matchByName = true;
            if (name == null)
                matchByName = false;
            if (size < 0)
                size = history.size();
            else
                size = Math.min(history.size(), size);
            LinkedList<InvokeInfo> targetInfos = new LinkedList<>();
            ListIterator<InvokeInfo> it = history.listIterator(history.size());
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
            return this.history;
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

    @FunctionalInterface
    public interface DestroyResources {

        void shutdown();

    }

}
