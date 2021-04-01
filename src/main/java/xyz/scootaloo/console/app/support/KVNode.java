package xyz.scootaloo.console.app.support;

/**
 * 键值对
 * @deprecated 暂时用不到
 * @author flutterdash@qq.com
 * @since 2021/3/9 10:53
 */
@Deprecated
public class KVNode<K, V> {
    private K key;
    private V value;

    public static <K, V> KVNode<K, V> of(K key, V value) {
        return new KVNode<>(key, value);
    }

    public KVNode() {
    }

    public KVNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KVNode{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
