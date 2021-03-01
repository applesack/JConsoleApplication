package xyz.scootaloo.test;

import org.junit.jupiter.api.Test;
import xyz.scootaloo.console.app.error.CommandInvokeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static xyz.scootaloo.console.app.util.InvokeProxy.fun;

/**
 * @author flutterdash@qq.com
 * @since 2021/2/28 10:16
 */
public class FunctionTest {

    @Test
    public void test0() {
        Optional<Integer> count = fun(this::rtn2p)
                .addHandle(IllegalArgumentException.class, ex -> {
                    System.out.println("Ill异常");
                }).addHandle(CommandInvokeException.class, (ex) -> {
                    System.out.println("方法调用异常 " + ex.getObj());
                    System.out.println(ex.getInput());
                })
                .getOptional(new HashMap<>(), 3);
//                .call();
        System.out.println(count);
    }

    public void nonRtn0p() {
    }

    public int rtn2p(Map<String, String> map, int a) {
        if (a == 3)
            throw new CommandInvokeException("xx");
        if (map.isEmpty())
            throw new IllegalArgumentException("1212");
        return map.size();
    }

    @Test
    public void test1() {
        int rsl = fun(this::exFun).setDefault(0).call(1, '1');
        System.out.println(rsl);
    }

    public int exFun(int a, char b) throws Exception {
        if (a == 2)
            throw new Exception();
        return a + b;
    }

}
