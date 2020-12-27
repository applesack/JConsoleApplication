package xyz.scootaloo.console.app.support.utils;

/**
 * @author flutterdash@qq.com
 * @since 2020/12/27 17:08
 */
public class ClassUtils {

    /**
     * 判断一个类是否是另一个类的子类
     * @param son 子类
     * @param father 父类
     * @return 结果
     */
    public static boolean isExtendForm(Object son, Class<?> father) {
        return father.isAssignableFrom(son.getClass()) || son.getClass().isInstance(father);
    }

}
