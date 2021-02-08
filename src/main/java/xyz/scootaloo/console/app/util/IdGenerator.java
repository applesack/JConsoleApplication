package xyz.scootaloo.console.app.util;

/**
 * Id 生成器
 * 通过自增的方式产生id
 * @author flutterdash@qq.com
 * @since 2021/2/7 20:16
 */
@Deprecated
public final class IdGenerator {
    // 16 进制码表
    private static final char[] table = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                            'A', 'B', 'C', 'D', 'E', 'F'};
    // 数字长度不够时用于补齐
    private static final String[] lack_zeros = {"", "0", "00", "000"};

    private long id;
    private final StringBuilder sb;

    // 长度为3的16进制Id
    public String getHexSize3() {
        sb.setLength(0);
        long num = increase();
        System.out.print(num + " ");
        while (num > 0) {
            long tmp = num;
            num >>= 4; // 除 16
            num <<= 4; // 乘 16
            num = tmp - num;
            sb.insert(0, table[(int) num]);
            num = tmp;
            num >>= 4;
        }
        int lacks = 3 - sb.length();
        if (lacks > 0)
            sb.insert(0, lack_zeros[lacks]);
        return sb.toString();
    }

    public long get() {
        return increase();
    }

    private long increase() {
        return id++;
    }

    public long reset() {
        return id = 0;
    }

    public static IdGenerator create() {
        return create(0);
    }

    public static IdGenerator create(long start) {
        return new IdGenerator(start);
    }

    // constructor
    private IdGenerator(long start) {
        this.id = start;
        this.sb = new StringBuilder();
    }

}
