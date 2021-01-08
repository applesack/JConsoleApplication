package xyz.scootaloo.console.app.workspace;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.*;
import xyz.scootaloo.console.app.support.listener.AppListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *   #####################################
 *  # 提示: 此类可以删除或者修改，不影响系统功能 #
 * ######################################
 *
 * --------- 登陆功能的简易实现 ------------
 * login #
 * reg #
 * logout
 * -------------------------------------
 *
 * @author flutterdash@qq.com
 * @since 2020/12/30 23:06
 */
public class LoginDemo implements AppListenerAdapter,
        Colorful { // 本系统可以使用实现接口的方式，简洁的调用工具类的方法

    private final Map<String, User> userMap;
    private String curCmd;
    private boolean hasLogin = false;

    // 提供public的无参构造方法
    public LoginDemo() {
        userMap = new HashMap<>();
    }

    @Cmd(type = CmdType.Init)
    private void loadUsers() {
        // 加载用户数据，可以修改成其他实现方式，例如从数据库中或者文件中获取
        // 这里向map中预先存储3个用户
        userMap.put("user1", new User("user1", "pwd"));
        userMap.put("admin", new User("admin", "admin"));
        userMap.put("test", new User("test", "admin"));
    }

    @Cmd(type = CmdType.Pre,onError = "未登陆，无法执行此操作")
    private boolean checkLogin() {
        // 保存一个允许放行的集合
        Set<String> allowOptions = Stream.of("reg", "register", "login", "help").collect(Collectors.toSet());
        // 当前用户未登陆，但是访问了需要登陆才能执行的操作时，拦截
        return this.hasLogin || allowOptions.contains(this.curCmd);
    }

    // 简易的登陆判断
    @Cmd(name = "log")
    private void login(User user) {
        User userInfo = userMap.get(user.username);
        if (userInfo == null) {
            println("用户名不存在");
        } else {
            if (userInfo.password.equals(user.password)) {
                this.hasLogin = true;
                println("登陆成功");
            } else {
                println("密码错误!");
            }
        }
    }

    // 简易的注册功能
    @Cmd(name = "reg")
    private void register(User user) {
        User userInfo = userMap.get(user.username);
        if (userInfo != null) {
            println("用户名已存在");
        } else {
            userMap.put(user.username, user);
            println("注册成功!");
        }
    }

    // 退出登陆
    @Cmd
    private void logout() {
        hasLogin = false;
        println("退出登陆状态");
    }

    //------------------启用监听器，辅助登陆检查功能实现-----------------------

    @Override
    public String getName() {
        return "loginPlg";
    }

    @Override
    public boolean accept(Moment moment) {
        return Moment.OnInput == moment; // 监听用户输入的命令
    }

    @Override
    public String onInput(String cmdline) {
        // 在用户输入之前进行拦截，此方法将在过滤器之间执行，保存用户当前执行的命令名
        String[] items = cmdline.split(" ");
        this.curCmd = items.length == 0 ? null : items[0];
        return cmdline;
    }

    //---------------------------表单---------------------------------

    @Form
    private static class User {

        @Prop(prompt = "输入用户名", isRequired = true)
        private String username;

        @Prop(prompt = "输入密 码", isRequired = true)
        private String password;

        public User() {
        }

        public User(String username, String password) {
            this.username = username;
            this.password = password;
        }

    }

}
