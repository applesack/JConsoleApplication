package xyz.scootaloo.console.app.parser.preset;

import xyz.scootaloo.console.app.anno.Opt;
import xyz.scootaloo.console.app.anno.mark.Stateless;
import xyz.scootaloo.console.app.parser.*;
import xyz.scootaloo.console.app.util.ClassUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 实验性功能
 *
 * 专用于解析数据结构的解析器，标记 collection
 *
 * 支持几种基本的数据结构，python风格，数据结构之间不支持嵌套
 * 数组/列表 key=[1, 2, 3, 4]
 * Set key=(1, 2, 3)
 * 映射 key={key1:value1, key2:value2}
 *
 * @author flutterdash@qq.com
 * @since 2021/3/8 0:04
 */
@Stateless
public final class CollectionParameterParser implements NameableParameterParser {
    /** singleton */
    protected static final CollectionParameterParser INSTANCE = new CollectionParameterParser();

    @Override
    public String name() {
        return "collection";
    }

    @Override
    public ResultWrapper parse(MethodMeta meta, String args) throws Exception {
        /*
         * Step 1.
         * 首先拿到命令行的参数，拿到的参数通常是这样的: a={'a b' : 12, 23:23, 90:k} [' x '] c=(1, 1, 2, 2)
         * 这里将对这样的数据进行处理，使用int数组对这些数据进行描述
         * int[] mark = new int[3];
         * mark[0]: 数据结构的类型，有三种数据结构 '{'代表Map=2, '('代表Set=0, '['代表List=1
         * mark[1]: 数据结构的起始边界，例如示例的命令行中， '{'第一次出现是下标为3的位置，所以 mark[1] ==> 2
         * mark[2]: 数据结构的结束边界，示例命令行中，'}'做为Map集合的结束边界，它出现在26，所以 mark[2] ==> 26
         *
         * 上面的示例命令行参数解析完成后，将会得到如下结果
         * boundMarkList.size() == 3
         * get(0) ==> [2, 2, 26]
         * get(1) ==> [1, 28, 34]
         * get(2) ==> [0, 38, 49]
         */
        List<int[]> boundMarkList = new ArrayList<>();
        boolean isOpen = false;
        int[] markSegment = {0, 0, 0}; // 标记段大小为3， 分别代表： 数据结构的类型，数据结构开始标记，数据结构结束标记
        for (int i = 0; i< args.length(); i++) {
            char point = args.charAt(i);
            if (!isOpen) {
                int type = isOpenSign(point);
                if (type >= 0) {
                    markSegment[1] = i;
                    isOpen = true;
                    markSegment[0] = type;
                }
            } else {
                if (isCloseSign(point, markSegment[0])) {
                    isOpen = false;
                    markSegment[2] = i;
                    boundMarkList.add(markSegment);
                    markSegment = new int[] {0, 0, 0};
                }
            }
        }

        /*
         * Step 2.
         * a. 定义一个具有解析功能的类 CollectionParser, 委托它来实现字符串到java数据结构的解析功能
         *    创建一个 CollectionParser 对象需要两个参数
         *        type: 类型，表示一段字符串将使用什么方式处理，这个在 Step1中已经可以得到这个了这个数据
         *        segment: 表示数据结构的字符串，在Step1中的示例命令行参数中，有3个数据结构，分别是
         *                          {'a b' : 12, 23:23, 90:k}
         *                          [' x ']
         *                          (1, 1, 2, 2)
         *                可以根据Step1中的信息取出这些子串。
         *
         * b. 现在还需要一个信息是参数名，在示例命令行参数中
         *              a={'a b' : 12, 23:23, 90:k} [' x '] c=(1, 1, 2, 2)
         *             |--|                       |—|     |---|
         *    根据Step1中的信息划几个区间，从这些区间(上一行横线的位置)中找到和方法参数注解匹配的参数名，假如一个方法是这样的
         *    public void test(@Opt('a') Map<String, String> map,
         *                     @Opt('b') List<Long> list,
         *                     @Opt('c') Set<Integer> set) {
         *    }
         *    这样的一个方法，会有参数 a 和 c 能和匹配上，但是b是缺省的，于是将以及处理完的信息划分为两个集合
         *
         * c. 划分出两个集合, 其中Map中存放能匹配上参数名的集合，List存放不能匹配参数名的集合
         *    KeyMap ==> {
         *         "a" : "{'a b' : 12, 23:23, 90:k}",
         *         "c" : "(1, 1, 2, 2)"
         *    }
         *    RemainList ==> ["[' x ']"]
         */
        Set<String> keyNames = collectNames(meta.optionals);
        List<CollectionParser> collectionParsers = new ArrayList<>();
        Map<String, CollectionParser> keyMap = new HashMap<>();
        for (int pos = 0; pos<boundMarkList.size(); pos++) {
            int[] segment = boundMarkList.get(pos);
            int rightBound = segment[1];
            int leftBound = pos == 0 ? 0 : boundMarkList.get(pos - 1)[2] + 1;
            if (leftBound > rightBound)
                continue;
            Optional<String> nameOptional = searchName(args.substring(leftBound, rightBound), keyNames);
            CollectionParser parser = new CollectionParser(segment[0], args.substring(segment[1], segment[2] + 1));
            if (nameOptional.isPresent()) {
                keyMap.put(nameOptional.get(), parser);
            } else {
                collectionParsers.add(parser);
            }
        }

        /*
         * Step 3.
         * 在这个步骤中开始执行最重要的功能----将所获取的信息转换成方法需要的参数
         */
        List<Object> targetMethodArgs = new ArrayList<>(); // 目标方法实际需要的参数
        Optional<Opt>[] annoList = meta.optionals;         // 目标方法参数上的注解
        Class<?>[] paramList = meta.parameterTypes;        // 目标方法的参数列表
        Type[] paramListGenericInfo = meta.genericTypes;   // 目标方法参数列表的泛型信息
        for (int i = 0; i<paramList.length; i++) {
            Class<?> currentParamType = paramList[i];  // 当前方法参数的类型
            Type currentGenericType = paramListGenericInfo[i]; // 当前方法参数的泛型类型
            Optional<Opt> optOptional = annoList[i];   // 当前方法参数的注解
            // 假如当前参数位置有注解
            if (optOptional.isPresent()) {
                Optional<CollectionParser> collMarkOptional = Optional.ofNullable(keyMap
                        .get(getNameStrategy(optOptional.get())));
                if (collMarkOptional.isPresent()) {
                    addMethodParamByType(targetMethodArgs, collMarkOptional.get().getConverter(),
                            currentParamType, currentGenericType);
                } else {
                    putIfExist(collectionParsers, targetMethodArgs,
                            currentParamType, currentGenericType);
                }
            }
            // 当前参数位置没有注解
            else {
                putIfExist(collectionParsers, targetMethodArgs,
                        currentParamType, currentGenericType);
            }
        }

        // 返回结果
        return ParameterWrapper.success(targetMethodArgs);
    }

    private void putIfExist(List<CollectionParser> collectionParsers, List<Object> targetMethodArgs,
                            Class<?> currentParamType, Type currentGenericType) throws ClassNotFoundException {
        // 没有参数解析器列表是空的，也就是没有信息可用，这里只能放置一个空集合
        if (collectionParsers.isEmpty()) {
            // 放置默认集合
            putEmptyCollection(targetMethodArgs, currentParamType);
        }
        // 不为空，按照顺序将第一个数据项的信息转换到这个位置来
        else {
            CollectionParser collMark = collectionParsers.remove(0);
            addMethodParamByType(targetMethodArgs, collMark.getConverter(),
                    currentParamType, currentGenericType);
        }
    }

    private void putEmptyCollection(List<Object> methodArgs, Class<?> type) {
        if (type.isArray()) {
            methodArgs.add(ClassUtils.genArray(type.getComponentType(), ""));
            return;
        }
        if (type == Set.class) {
            methodArgs.add(new LinkedHashSet<>(0));
            return;
        }
        if (type == List.class) {
            methodArgs.add(new ArrayList<>());
            return;
        }
        if (type == Map.class) {
            methodArgs.add(new HashMap<>(0));
        } else {
            throw new RuntimeException("不支持的集合类型");
        }
    }

    private void addMethodParamByType(List<Object> methodArgs, Converter converter,
                                      Class<?> type, Type genericType) throws ClassNotFoundException {
        if (type.isArray()) {
            methodArgs.add(converter.toArray(type.getComponentType()));
            return;
        }
        List<Class<?>> realTypes = ClassUtils.getRawType(genericType);
        if (type == Set.class) {
            methodArgs.add(converter.toSet(realTypes.get(0)));
            return;
        }
        if (type == List.class) {
            methodArgs.add(converter.toList(realTypes.get(0)));
            return;
        }
        if (type == Map.class) {
            methodArgs.add(converter.toMap(realTypes.get(0), realTypes.get(1)));
        } else {
            throw new RuntimeException("不支持的集合类型");
        }
    }

    private Optional<String> searchName(String interval, Set<String> nameSet) {
        for (String name : nameSet) {
            if (interval.contains(name))
                return Optional.of(name);
        }
        return Optional.empty();
    }

    private Set<String> collectNames(Optional<Opt>[] optionals) {
        Set<String> rsl = new LinkedHashSet<>();
        for (Optional<Opt> optOptional : optionals) {
            optOptional.ifPresent(opt -> rsl.add(getNameStrategy(opt)));
        }
        return rsl;
    }

    private String getNameStrategy(Opt opt) {
        if (!opt.fullName().isEmpty()) {
            return opt.fullName();
        } else {
            return String.valueOf(opt.value());
        }
    }

    private int isOpenSign(char point) {
        char[] openSign = {'(', '[', '{'};
        for (int i = 0; i<openSign.length; i++) {
            if (point == openSign[i])
                return i;
        }
        return -1;
    }

    private boolean isCloseSign(char point, int pos) {
        char[] closeSign = {')', ']', '}'};
        return point == closeSign[pos];
    }

    @Override
    public String toString() {
        return getParserString();
    }

    private static class CollectionParser {
        private final Converter converter;
        public CollectionParser(int type, String segment) {
            this.converter = switchConverter(type);
            this.converter.init(segment);
        }

        public Converter getConverter() {
            return this.converter;
        }

        private Converter switchConverter(int type) {
            switch (type) {
                case 1: return new ListConverter();
                case 2: return new MapConverter();
                default:
                    return new SetConverter();
            }
        }
    }

    /**
     * 转换器
     * 提供统一的获取数据项的方法
     */
    public abstract static class Converter {
        abstract void init(String rawString);
        abstract <T> Set<T> toSet(Class<T> type);
        abstract <T> List<T> toList(Class<T> type);
        abstract <T> T[] toArray(Class<T> type);
        abstract <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType);

        /**
         * 将字符串拆解成数据项列表
         * @param rawString 代表数据结构的字符串
         * @param bound 边界标记
         * @return 数据项集合
         */
        protected static List<String> collect(String rawString, char bound) {
            List<String> collection = new ArrayList<>();
            StringBuilder tmp = new StringBuilder();
            int len = rawString.length();
            boolean isOpen = false;
            for (int i = 1; i<len; i++) {
                char c = rawString.charAt(i);
                if (c == '\'' || c == '"') {
                    if (isOpen) {
                        collection.add(tmp.toString());
                        tmp.setLength(0);
                        isOpen = false;
                    } else {
                        tmp.setLength(0);
                        isOpen = true;
                    }
                    continue;
                }
                if (isOpen) {
                    tmp.append(c);
                    continue;
                }
                if (c == ' ')
                    continue;
                if (c == ',' || c == bound) {
                    if (tmp.length() != 0) {
                        collection.add(tmp.toString());
                        tmp.setLength(0);
                    }
                } else {
                    tmp.append(c);
                }
            }
            return collection;
        }

        /**
         * 将字符串解析成Map集合
         * @param rawString 代表数据结构的字符串
         * @return 解析完成的Map集合
         */
        protected static Map<String, String> collectMap(String rawString) {
            final char bound = '}';
            final String delimiter = ":";
            StringBuilder tmp = new StringBuilder();
            int len = rawString.length();
            for (int i = 0; i<len; i++) {
                char c = rawString.charAt(i);
                if (c == ':') {
                    tmp.append(",:,");
                } else {
                    tmp.append(c);
                }
            }
            List<String> segments = collect(tmp.toString(), bound);
            Map<String, String> collection = new HashMap<>();
            Iterator<String> segIterator = segments.listIterator();
            while (segIterator.hasNext()) {
                String key = segIterator.next();
                if (segIterator.hasNext()) {
                    if (segIterator.next().equals(delimiter)) {
                        if (segIterator.hasNext()) {
                            String value = segIterator.next();
                            collection.put(key, value);
                        }
                    } else {
                        throw new RuntimeException("出现错误的分隔符: `" + rawString + "`");
                    }
                }
            }
            return collection;
        }

    }

    /**
     * List集合向其他集合的转换实现
     */
    public static class ListConverter extends Converter {

        private final ThreadLocal<List<String>> localList = new ThreadLocal<>();

        @Override
        void init(String rawString) {
            localList.set(collect(rawString, ']'));
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> Set<T> toSet(Class<T> type) {
            Set<T> targetSet = new LinkedHashSet<>();
            for (String seg : localList.get())
                targetSet.add((T) TransformFactory.simpleTrans(seg, type));
            return targetSet;
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> List<T> toList(Class<T> type) {
            List<T> targetList = new ArrayList<>();
            for (String seg : localList.get())
                targetList.add((T) TransformFactory.simpleTrans(seg, type));
            return targetList;
        }

        @Override
        <T> T[] toArray(Class<T> type) {
            return ClassUtils.genArray(type, localList.get());
        }

        @Override
        <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType) {
            throw new RuntimeException("不支持的类型转换: List -> Map");
        }

    }

    /**
     * Set 集合向其他数据结构的转换实现
     */
    public static class SetConverter extends Converter {

        private final Set<String> dataSet = new LinkedHashSet<>();

        @Override
        void init(String rawString) {
            dataSet.clear();
            dataSet.addAll(collect(rawString, ')'));
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> Set<T> toSet(Class<T> type) {
            Set<T> targetSet = new LinkedHashSet<>();
            for (String seg : dataSet)
                targetSet.add((T) TransformFactory.simpleTrans(seg, type));
            return targetSet;
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> List<T> toList(Class<T> type) {
            List<T> targetList = new ArrayList<>();
            for (String seg : dataSet)
                targetList.add((T) TransformFactory.simpleTrans(seg, type));
            return targetList;
        }

        @Override
        <T> T[] toArray(Class<T> type) {
            return ClassUtils.genArray(type, dataSet);
        }

        @Override
        <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType) {
            throw new RuntimeException("不支持的类型转换: Set -> Map");
        }

    }

    /**
     * Map集合向其他数据结构转换的实现
     */
    public static class MapConverter extends Converter {

        private final Map<String, String> dataMap = new HashMap<>();

        @Override
        void init(String rawString) {
            dataMap.clear();
            dataMap.putAll(collectMap(rawString));
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> Set<T> toSet(Class<T> type) {
            Set<String> segSet = dataMap.keySet();
            Set<T> targetSet = new LinkedHashSet<>();
            for (String str : segSet) {
                targetSet.add((T) TransformFactory.simpleTrans(str, type));
            }
            return targetSet;
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <T> List<T> toList(Class<T> type) {
            Set<String> segSet = dataMap.keySet();
            List<T> targetSet = new ArrayList<>();
            for (String str : segSet) {
                targetSet.add((T) TransformFactory.simpleTrans(str, type));
            }
            return targetSet;
        }

        @Override
        <T> T[] toArray(Class<T> type) {
            return ClassUtils.genArray(type, dataMap.keySet());
        }

        @Override
        @SuppressWarnings({ "unchecked", "hiding" })
        <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType) {
            Map<K, V> targetMap = new HashMap<>();
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                K targetKey = (K) TransformFactory.simpleTrans(entry.getKey(), keyType);
                V targetValue = (V) TransformFactory.simpleTrans(entry.getValue(), keyType);
                targetMap.put(targetKey, targetValue);
            }
            return targetMap;
        }

    }

}
