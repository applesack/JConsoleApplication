package xyz.scootaloo.test;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author flutterdash@qq.com
 * @since 2021/2/16 17:23
 */
public class ReflectionTest {

    public static void main(String[] args) {
        Arrays.stream(getMethods(getInstance()))
                .map(ReflectionTest::showMethodInfo)
                .forEach(System.out::println);
    }

    public static String showMethodInfo(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName()).append('(');
        Type rtnGeneric = method.getGenericReturnType();
        Type[] paramGenerics = method.getGenericParameterTypes();
        sb.append(Arrays.stream(paramGenerics)
                .map(ReflectionTest::typeSimpleView)
                .collect(Collectors.joining(","))
        ).append(')').append(':').append(typeSimpleView(rtnGeneric));
        return sb.toString();
    }

    public Map<String, List<String>> getProp(List<Integer> list, ArrayList<Double> doubles, Date date) {
        return null;
    }

    public void getMap(Map<String, Double> map) {
    }

    private static Method[] getMethods(Object obj) {
        Class<?> clazz = obj.getClass();
        return clazz.getDeclaredMethods();
    }

    private static ReflectionTest getInstance() {
        return new ReflectionTest();
    }

    private static String typeSimpleView(Type type) {
        return typeSimpleView(type.toString());
    }

    private static String typeSimpleView(String typeStr) {
        typeStr = typeStr.replace("class ", "");
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i<typeStr.length(); i++) {
            char c = typeStr.charAt(i);
            if (c == ' ' || c == ';')
                continue;
            tmp.append(c);
        }
        typeStr = tmp.toString();
        if (typeStr.indexOf('<') == -1) {
            return getSimpleName(typeStr);
        }
        tmp.setLength(0);
        StringBuilder rsl = new StringBuilder();

        for (int i = 0; i<typeStr.length(); i++) {
            char c = typeStr.charAt(i);
            if (c == '<' || c == '>' || c == ',') {
                if (tmp.length() != 0 && tmp.charAt(tmp.length() - 1) == ';')
                    tmp.setLength(tmp.length() - 1);
                rsl.append(getSimpleName(tmp.toString())).append(c);
                tmp.setLength(0);
            } else {
                tmp.append(c);
            }
        }
        if (tmp.length() > 0)
            rsl.append(tmp);
        return rsl.toString();
    }

    private static String getSimpleName(String str) {
        boolean isArray = str.startsWith("[");
        int pointIdx = str.lastIndexOf(".");
        if (pointIdx == -1 || pointIdx == str.length() - 1) {
            return str + (isArray ? "[]" : "");
        } else {
            return str.substring(pointIdx + 1) + (isArray ? "[]" : "");
        }
    }

    @Test
    public void testSimpleView() {
        String typeStr = "java.util.Map<[Ljava.lang.String;, java.lang.Double>";
        String typeStr1 = "class [Ljava.lang.String;";
        System.out.println(typeSimpleView(typeStr));
        System.out.println(typeSimpleView(typeStr1));
    }

    @Test
    public void testGetSimpleName() {
        String typeStr = "xyz.scootaloo.Main";
        System.out.println(getSimpleName(typeStr));
    }

}
