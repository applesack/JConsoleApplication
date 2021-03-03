package xyz.scootaloo.test.thread;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.util.IdGenerator;

/**
 * @author flutterdash@qq.com
 * @since 2021/3/2 18:20
 */
public class IdTest {

    @Test
    public void test() {
        IdGenerator idGenerator = IdGenerator.create();
        for (int i = 0; i<30; i++) {
            System.out.println(idGenerator.get());
        }
    }

}
