package xyz.scootaloo.console.app.support.parser;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 转换器接口
 * 一条命令有命令名和参数构成
 * 转换器的任务是将命令参数转换成方法的参数
 * @author flutterdash@qq.com
 * @since 2021/1/16 22:59
 */
@FunctionalInterface
public interface ParameterParser {

    /**
     *
     * @param method 需要提供参数的方法对象
     * @param arg 命令参数每段都用空格分隔，这里已经预先分隔成了列表
     * @return 一个包装类，包含处理的结果
     */
    Wrapper parse(Method method, List<String> arg);

    /**
     * 编写自定义参数解析器实现的时候，可以重写这个方法，检查方法参数是否符合要求，以便在运行之初抛出异常方便检查
     * @param method 方法
     * @return 是否符合所需的规范
     */
    default boolean check(Method method) {
        return true;
    }

}
