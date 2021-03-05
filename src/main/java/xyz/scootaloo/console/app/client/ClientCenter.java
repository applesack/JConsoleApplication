package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.parser.Interpreter;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 12:14
 */
public final class ClientCenter {
    private final Client PUBLIC_SPACE = new Client("ROOT");
    protected static volatile ClientCenter SINGLETON;
    private final Map<String, Client> users;
    protected final int maxHistory;

    protected ClientCenter(Interpreter interpreter) {
        this.users = new ConcurrentHashMap<>();
        this.maxHistory = interpreter.getConfig().getMaxHistory();
        this.users.put(PUBLIC_SPACE.userKey, PUBLIC_SPACE);
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

    @Deprecated
    public static void show() {
        // 在此处插入断点，观察用户状态
        System.out.println();
    }

    /**
     * 根据用户标识创建一个用户对象，假如此用户标识已经存在，则拿到已存在的用户对象，否则拿到新创建出来的对象
     * @param userKey 用户标识，必须是唯一的
     * @return 用户对象，可使用此对象管理资源
     */
    public Client createUser(String userKey) {
        Client user, existUser;
        synchronized (SINGLETON.users) {
            user = new Client(userKey);
            existUser = SINGLETON.users.putIfAbsent(userKey, user);
            if (existUser != null)
                user = existUser;
        }
        return user;
    }

    public Client getPublicUser() {
        return PUBLIC_SPACE;
    }

    protected static void destroyUserResources(String userKey) {
        synchronized (SINGLETON.users) {
            Optional.ofNullable(SINGLETON.users.remove(userKey))
                    .ifPresent(Client::destroy);
        }
    }

}
