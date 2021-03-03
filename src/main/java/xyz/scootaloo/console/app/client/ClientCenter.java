package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.util.IdGenerator;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 12:14
 */
public class ClientCenter {

    private final IdGenerator idGenerator;

    public ClientCenter() {
        this.idGenerator = IdGenerator.create();
    }


    /**
     * 根据当前线程的信息给当前用户分配一些资源
     * @return 此用户的标签，可根据此标签访问指定的资源
     */
    public String getUserKey() {
        return "";
    }

    public String getUserKey(String s) {
        return "";
    }

    /**
     * 根据用户标签销毁给此用户分配过的资源。
     * <p>此操作需要具有ROOT权限</p>
     * @param userKey 用户标签
     */
    public void destroyUserData(String userKey) {

    }

    /**
     * 定义一个连接的用户信息
     * @author flutterdash@qq.com
     * @since 2021/3/2 17:52
     */
    private static class User {
        final String uniqueKey;
        final String userKey;
        final Thread thread;

        public User(Thread thread, String uniqueKey, String userKey) {
            this.thread = thread;
            this.userKey = userKey;
            this.uniqueKey = uniqueKey;
        }

        @Override
        public int hashCode() {
            return uniqueKey.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (!(obj instanceof User))
                return false;
            User other = (User) obj;
            return other.uniqueKey.equals(this.uniqueKey);
        }

    }

}
