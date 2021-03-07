package xyz.scootaloo.console.app.client;

import xyz.scootaloo.console.app.util.IdGenerator;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 命令行占位符替换记录
 *
 * @author flutterdash@qq.com
 * @since 2021/3/6 12:09
 */
public class ReplacementRecord {
    private final Set<KVPair> records;
    private static final IdGenerator idGenerator = IdGenerator.create();

    protected ReplacementRecord() {
        this.records = new LinkedHashSet<>();
    }

    public void add(KVPair kvPair) {
        records.add(kvPair);
    }

    public Set<KVPair> getRecords() {
        return this.records;
    }

    public void refresh() {
        records.clear();
        idGenerator.reset();
    }

    /**
     * <pre>命令行中占位符的描述
     * 当开启了变量功能时，解释器每次读取命令行，都会尝试将命令行中的占位符替换成实际的变量.
     * 在解释器处理变量与占位符时，会有这样的流程。
     *     1. 通过预置的命令，或者其他方式，将变量存储到用户的变量池中
     *     2. 在上游部分，对命令行进行处理的替换。
     *        每个占位符假如有一个变量与之对应，则它会被原地替换成一个标记，例如占位符命令行中 ${abc} 可能被替换成 @#@12
     *        这个12是这次替换记录的一个id，它是唯一的。
     *     3. 下游部分，接受到了 @#@12 这样的标记，于是在在这个位置查找对应的替换记录，得到实际的值
     *     over 完成占位符替换功能
     * </pre>
     * @author flutterdash@qq.com
     * @since 2021/3/6 15:06
     */
    public static class KVPair {

        public boolean hasVar;    // 是否有变量
        public String key;        // 此变量的key
        public Object value;      // 此变量的值
        public final Integer id;  // 唯一ID

        public KVPair() {
            // 被创建时，更新id
            this.id = idGenerator.get().intValue();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KVPair kvPair = (KVPair) o;
            return Objects.equals(id, kvPair.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

    }

}
