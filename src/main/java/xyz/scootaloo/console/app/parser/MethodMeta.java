package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将method中的有效信息收集起来，避免重复计算
 * 注意，这些集合会被反复使用，所以，不要修改这些集合的内容
 * @author flutterdash@qq.com
 * @since 2021/2/7 21:42
 */
public final class MethodMeta {
    public final Object obj;                 // 此方法所属的类
    public final Method method;              // method 反射方法对象
    public final int size;                   // 方法参数个数
    public final Optional<Opt>[] optionals;  // 方法中的Opt注解数组，数组的下标代表第i个参数的注解
    public final Class<?>[] parameterTypes;  // 参数的类型
    public final Type[] genericTypes;        // 可以使用Type查看该参数的泛型类型
    public final Set<Character> optCharSet;  // 短参数集
    public final Set<String> fullNameSet;    // 完整参数集
    public final Set<String> jointMarkSet;   // 连接标记集

    // constructor
    private MethodMeta(Method method, Object obj) {
        this.obj = obj;
        this.method = method;
        this.size = method.getParameterCount();
        this.optionals = findOption();
        this.parameterTypes = method.getParameterTypes();
        this.genericTypes = method.getGenericParameterTypes();
        this.optCharSet = findOptCharSet();
        this.fullNameSet = findFullNameSet();
        this.jointMarkSet = findJointMartSet();
    }

    // 获取此方法的元数据
    protected static MethodMeta getInstance(Method method, Object obj) {
        return new MethodMeta(method, obj);
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    private Optional<Opt>[] findOption() {
        Annotation[][] anno2DArr = this.method.getParameterAnnotations();
        int size = this.method.getParameterCount();
        Optional<Opt>[] opts = new Optional[size];
        for (int i = 0; i<size; i++) {
            opts[i] = findOption(anno2DArr[i]);
        }
        return opts;
    }

    private Set<Character> findOptCharSet() {
        return Arrays.stream(this.optionals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Opt::value)
                .collect(Collectors.toSet());
    }

    private Set<String> findFullNameSet() {
        return Arrays.stream(this.optionals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Opt::fullName)
                .filter(fullName -> !fullName.isEmpty())
                .collect(Collectors.toSet());
    }

    private Set<String> findJointMartSet() {
        return Arrays.stream(this.optionals)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Opt::joint)
                .flatMap(opt -> Stream.of(String.valueOf(opt.value()), opt.fullName()))
                .filter(str -> !str.isEmpty())
                .collect(Collectors.toSet());
    }

    private Optional<Opt> findOption(Annotation[] annoArr) {
        for (Annotation anno : annoArr) {
            if (anno.annotationType() == Opt.class)
                return Optional.of((Opt) anno);
        }
        return Optional.empty();
    }

}
