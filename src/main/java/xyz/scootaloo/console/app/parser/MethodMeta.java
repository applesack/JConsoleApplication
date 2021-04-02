package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.error.ConsoleAppRuntimeException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 将method中的有效信息收集起来，避免重复计算<br>
 * 这些集合会被反复使用，所以，不要修改这些集合的内容
 *
 * @author flutterdash@qq.com
 * @since 2021/2/7 21:42
 */
public final class MethodMeta implements Iterable<MethodMeta.CurrentParamInfo> {
    public final          Object owner;            // 此方法所属的类
    public final          Method method;           // method 反射方法对象
    public final             int size;             // 方法参数个数
    public final Optional<Opt>[] optionals;        // 方法中的Opt注解数组，数组的下标代表第i个参数的注解
    public final      Class<?>[] parameterTypes;   // 参数的类型
    public final          Type[] genericTypes;     // 可以使用Type查看该参数的泛型类型
    public final  Set<Character> optCharSet;       // 短参数集
    public final     Set<String> fullNameSet;      // 完整参数集
    public final     Set<String> jointMarkSet;     // 连接标记集

    // constructor
    private MethodMeta(Method method, Object obj) {
        this.owner = obj;
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
    public static MethodMeta getInstance(Method method, Object obj) {
        return new MethodMeta(method, obj);
    }

    @Override
    public Iterator<CurrentParamInfo> iterator() {
        return new CurrentTypeIterator(this);
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

    @Override
    public String toString() {
        return "MethodMeta{" +
                "owner=" + owner +
                ", method=" + method +
                ", size=" + size +
                ", optionals=" + Arrays.toString(optionals) +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                ", genericTypes=" + Arrays.toString(genericTypes) +
                ", optCharSet=" + optCharSet +
                ", fullNameSet=" + fullNameSet +
                ", jointMarkSet=" + jointMarkSet +
                '}';
    }

    public static class CurrentParamInfo {
        private static final String ON_ERROR = "错误的用法";
        private final Optional<Opt> optionalOpt;
        private final Class<?> paramType;
        private final Type genericType;
        private final boolean joint;
        private final int index;
        public CurrentParamInfo(MethodMeta meta, int idx) {
            this.optionalOpt = meta.optionals[idx];
            this.paramType = meta.parameterTypes[idx];
            this.genericType = meta.genericTypes[idx];
            this.joint = hasJointMark(meta, this.optionalOpt);
            this.index = idx;
        }

        private boolean hasJointMark(MethodMeta meta, Optional<Opt> optionalOpt) {
            if (optionalOpt.isPresent()) {
                Opt opt = optionalOpt.get();
                return meta.jointMarkSet.contains(opt.fullName()) ||
                        meta.jointMarkSet.contains(String.valueOf(opt.value()));
            } else {
                return false;
            }
        }

        public boolean isJoint() {
            return joint;
        }

        public int index() {
            return index;
        }

        public boolean isRequired() {
            return getAnno().required();
        }

        public Class<?> getParamType() {
            return paramType;
        }

        public boolean hasOptAnno() {
            return optionalOpt.isPresent();
        }

        public Opt getAnno() {
            if (optionalOpt.isPresent())
                return optionalOpt.get();
            throw new RuntimeException(ON_ERROR);
        }

        public boolean hasDefaultValue() {
            if (!hasOptAnno())
                throw new RuntimeException(ON_ERROR);
            return !getAnno().dftVal().isEmpty();
        }

        public String getDefaultValue() {
            return getAnno().dftVal();
        }

        public Type getGenericType() {
            return genericType;
        }
    }

    private static class CurrentTypeIterator implements Iterator<CurrentParamInfo> {
        private final MethodMeta meta;
        private int currentIndex;
        private CurrentTypeIterator(MethodMeta meta) {
            this.meta = meta;
            currentIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < meta.size;
        }

        @Override
        public CurrentParamInfo next() {
            CurrentParamInfo currentType = new CurrentParamInfo(meta, currentIndex);
            currentIndex++;
            return currentType;
        }

    }

    // 缺省参数标记
    public static class LackMark {
        private final int idx;        // 缺省参数位于参数数组的下标
        private final Class<?> type;  // 对应方法参数的类型
        private final Type generic;   // 对应方法参数的泛型类型
        private final boolean joint;  // 是否需要拼接参数

        public LackMark(int index, Class<?> type) {
            this(index, type, null, false);
        }

        public LackMark(int index, Class<?> type, Type generic, boolean joint) {
            this.idx = index;
            this.type = type;
            this.generic = generic;
            this.joint = joint;
        }

        public int index() {
            return idx;
        }

        public Class<?> type() {
            return type;
        }

        public Type generic() {
            return generic;
        }

        public boolean isJoint() {
            return joint;
        }

    }

    /**
     * 中间状态
     * 在整个参数解析过程中，通过这个对象传递解析过程信息
     */
    public static class Context<T> {
        private final           MethodMeta meta;
        private final      Optional<Opt>[] optionals;       // 方法参数注解数组
        private final         List<Object> methodArgs;      // 待返回的参数列表
        private final  Map<String, String> kvPairs;         // 命令行中的参数键值对
        private final         List<String> remainList;      // 命令行移除了键值对后剩余的内容
        private final       List<LackMark> lackMarks;       // 当一个方法参数是缺省的，则被加入到这个集合
        private                          T infoObj;         // 使用另外的信息表示状态
        private ConsoleAppRuntimeException exception = null;

        private static final Object PLACEHOLDER = new Object();
        public static Context<Object> getInstance(MethodMeta meta,
                                                  String args,
                                                  Extractor<Object> extractor) {
            return getInstance(meta, args, extractor, PLACEHOLDER);
        }

        public static <T> Context<T> getInstance(MethodMeta meta,
                                                 String args,
                                                 Extractor<T> extractor,
                                                 T dft) {
            return new Context<>(meta, args, extractor, dft);
        }

        public Context(MethodMeta meta, String args, Extractor<T> extractor, T defaultValue) {
            this.meta = meta;
            optionals = meta.optionals;  // 方法参数的 Opt 注解数组
            methodArgs = new ArrayList<>(meta.size);
            kvPairs = new HashMap<>(8);
            remainList = extractor.extract(this, args);
            lackMarks = new ArrayList<>();
            infoObj = defaultValue;
        }

        public MethodMeta meta() {
            return meta;
        }

        public Map<String, String> getKVPairs() {
            return kvPairs;
        }

        public Optional<Opt>[] getOptionals() {
            return optionals;
        }

        public void addMethodArgument(Object arg) {
            methodArgs.add(arg);
        }

        public List<Object> getMethodArgs() {
            return methodArgs;
        }

        public boolean containParam(String paramName) {
            return kvPairs.containsKey(paramName);
        }

        public String getParam(String paramName) {
            return kvPairs.get(paramName);
        }

        public boolean hasException() {
            return exception != null;
        }

        public void setException(ConsoleAppRuntimeException exception) {
            this.exception = exception;
        }

        public ConsoleAppRuntimeException getException() {
            return exception;
        }

        public void addLackMark(LackMark lackMark) {
            this.lackMarks.add(lackMark);
        }

        public List<LackMark> getLackMarks() {
            return lackMarks;
        }

        public boolean hasLackMark() {
            return !lackMarks.isEmpty();
        }

        public List<String> getRemainList() {
            return remainList;
        }

        public void setInfo(T info) {
            this.infoObj = info;
        }

        public T getInfo() {
            return this.infoObj;
        }

    }

    @FunctionalInterface
    public interface Extractor<T> {

        List<String> extract(Context<T> state, String args);

    }

}
