package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.common.Factory;

import java.util.List;

/**
 * 参数解析器<br>
 * <p>这个接口的主要任务是根据命令行参数，得到java方法参数，每当有命令行输入，框架会先调用 {@code parser()} 方法，
 * 得到java方法的参数，然后再调用java方法。</p>
 * @author flutterdash@qq.com
 * @since 2021/1/16 22:59
 */
@FunctionalInterface
public interface ParameterParser extends Factory {

    /**
     * 根据命令行参数获取方法的参数
     * @param meta 目标java方法的 method 对象的一些信息，这些信息是根据 method 对象的数据收集并处理后得来的，使用这些信息可以减少计算量
     * @param args 命令参数每段都用空格分隔，这里已经预先分隔成了列表
     * @return 一个包装类，包含处理的结果
     * @throws Exception 解析时抛出的异常
     */
    ResultWrapper parse(MethodMeta meta, List<String> args) throws Exception;

    /**
     * 编写自定义参数解析器实现的时候，可以重写这个方法，检查方法参数是否符合要求，以便在运行之初抛出异常方便检查
     * @param meta 方法中的有效信息
     * @return 是否符合所需的规范，假如这里返回 false ，则这个方法不会被框架装配
     */
    default boolean check(MethodMeta meta) {
        return true;
    }

}
