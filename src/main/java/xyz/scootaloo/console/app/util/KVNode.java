package xyz.scootaloo.console.app.util;

/**
 * 键值对
 * @author flutterdash@qq.com
 * @since 2021/3/9 10:53
 */
public class KVNode<K, V> {

    private K key;
    private V value;

    public KVNode() {
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
