package xyz.scootaloo.console.app.parser;

import xyz.scootaloo.console.app.client.Console;
import xyz.scootaloo.console.app.anno.Form;
import xyz.scootaloo.console.app.anno.Prop;
import xyz.scootaloo.console.app.common.ResourceManager;
import xyz.scootaloo.console.app.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Scanner;

/**
 * 表单助手类，根据控制台的输入生成对象
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:34
 */
public final class FormHelper {
    // resources
    private static final Scanner scanner = ResourceManager.getScanner();
    private static final Console printer = ResourceManager.getConsole();

    /**
     * 检查输入类型是否符合表单类条件
     * @see ObjWrapper 包装类，假如结果可读则suceess属性为true
     *
     * @param formClazz 被检查的类
     * @return 如果是表单类，此类上应该含有 @Form 注解，因为程序需要读取退出命令
     * @throws IllegalAccessException -
     * @throws InstantiationException -
     * @throws InvocationTargetException -
     */
    public static ObjWrapper checkAndGet(Class<?> formClazz) throws IllegalAccessException,
                                                                    InstantiationException,
                                                                    InvocationTargetException {
        Form form = formClazz.getAnnotation(Form.class);
        if (form == null)
            return ObjWrapper.failed();
        String exitCmd = form.dftExtCmd();
        Object instance = ClassUtils.newInstance(formClazz);
        Field[] fields = formClazz.getDeclaredFields();
        for (int i = 0; i<fields.length; i++) {
            if (getAndSetProp(fields[i], instance, exitCmd, null, false) == -1) {
                // 跳转到下一个必选项
                i = gotoNextItem(fields, i) - 1;
                if (i < 0)
                    break;
            }
        }

        // 检查是否需要修改
        while (showProperties(instance, formClazz)) {
            modifyMode(instance, formClazz, exitCmd);
        }

        return ObjWrapper.success(instance);
    }

    /**
     * 将键盘输入的值设置入类属性
     * @param field 类属性
     * @param instance 此类属性对应的类实例
     * @param exitCmd 退出命令，有 @Form 注解提供
     * @param customPrompt 自定义的输入提示
     * @param isModifyMode 是否是修改模式，处于修改模式可以用空行退出
     * @return 退出循环时的状态
     */
    private static byte getAndSetProp(Field field, Object instance, String exitCmd,
                                      String customPrompt, boolean isModifyMode) {
        field.setAccessible(true);
        Prop prop = field.getAnnotation(Prop.class);
        if (prop == null)
            return -1;
        boolean isRequired = prop.isRequired();
        String prompt = customPrompt == null ? field.getName() : customPrompt;
        if (customPrompt == null && !prop.prompt().equals("")) {
            prompt = prop.prompt();
        }
        prompt += isRequired ? "! " : "~ ";

        /*
         * 非修改模式:
         *      有效的输入 1
         *      空行可以退出当前非必选参数输入 0
         *      退出命令可以退出当前输入，如果接下来没有必须参数，则退出，如果有，则跳转到输入下一个必须参数 -1
         * 修改模式：
         *      有效的输入 1
         *      空行可以跳过当前输入 0
         *      退出命令可以退出其余输入 -1
         */
        while (true) {
            printer.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals(exitCmd)) { // break
                if (isRequired && !isModifyMode)
                    continue;
                else
                    return  -1;
            }
            if (input.isEmpty() && isRequired && !isModifyMode)
                continue;
            if (input.isEmpty())
                return 0;
            try {
                field.set(instance, TransformFactory.parsingParam(input, field.getType(), field.getGenericType()));
                return 1;
            } catch (Exception e) {
                printer.println("属性值无效, 信息: " + e.getMessage());
            }
        }
    }

    /**
     * 跳转到下一个必选项的坐标，
     * @param fields -
     * @param curIdx -
     * @return 坐标，假如不存在下一个必选项，则返回0
     */
    private static int gotoNextItem(Field[] fields, int curIdx) {
        int nextIdx = curIdx + 1;
        int len = fields.length;
        if (nextIdx >= len)
            return -1;
        for (; nextIdx<len; nextIdx++) {
            Field field = fields[nextIdx];
            Prop prop = field.getAnnotation(Prop.class);
            if (prop == null)
                continue;
            if (prop.isRequired())
                return nextIdx;
        }
        return -1;
    }

    /**
     * 进入修改模式，可以逐个检查输入，或则输入退出命令退出输入
     * @param instance -
     * @param form -
     * @param extCmd -
     * @throws IllegalAccessException -
     */
    private static void modifyMode(Object instance, Class<?> form, String extCmd) throws IllegalAccessException {
        printer.println("进入编辑模式，无需改动则回车跳过，需要修改则输入新值覆盖旧值");
        StringBuilder sb = new StringBuilder();
        for (Field field : form.getDeclaredFields()) {
            sb.setLength(0);
            field.setAccessible(true);
            Object value = field.get(instance);
            String prompt = sb.append("[").append(field.getName()).append("\t")
                    .append(value).append("\t").append("] ").toString();
            if (getAndSetProp(field, instance, extCmd, prompt, true) == -1)
                break;
        }
    }

    /**
     * 展示一个表单类的全部带有 @Prop 注解的类属性 值信息
     * @param instance 类的示例
     * @param form 此实例的class对象
     * @return 是否需要重新检查
     * @throws IllegalAccessException -
     */
    private static boolean showProperties(Object instance, Class<?> form) throws IllegalAccessException {
        printer.println("以下是当前表单中的内容:");
        StringBuilder sb = new StringBuilder();
        for (Field field : form.getDeclaredFields()) {
            field.setAccessible(true);
            sb.setLength(0);
            Prop prop = field.getAnnotation(Prop.class);
            if (prop == null)
                continue;
            sb.append("[").append(field.getName()).append(":]")
                    .append(prop.isRequired() ? "! " : "~ ").append(field.get(instance));
            printer.println(sb);
        }
        printer.print("确认输入是否无误？Y(确认) N(需要改动，进入修改模式): ");
        String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        return !input.startsWith("y");
    }

    // 将对象结果进行包装
    public static class ObjWrapper {

        public final boolean success;
        public final Object instance;

        public ObjWrapper(boolean success, Object instance) {
            this.success = success;
            this.instance = instance;
        }

        public static ObjWrapper success(Object instance) {
            return new ObjWrapper(true, instance);
        }

        public static ObjWrapper failed() {
            return new ObjWrapper(false, null);
        }

    }

}
