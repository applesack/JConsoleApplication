package xyz.scootaloo.console.app.support.parser;

import xyz.scootaloo.console.app.support.common.Colorful;
import xyz.scootaloo.console.app.support.component.Form;
import xyz.scootaloo.console.app.support.component.Prop;
import xyz.scootaloo.console.app.support.component.ResourceManager;
import xyz.scootaloo.console.app.support.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Scanner;

/**
 * 表单助手类，根据控制台的输入生成对象
 * @author flutterdash@qq.com
 * @since 2020/12/31 16:34
 */
public class FormHelper {

    private static final Scanner scanner = ResourceManager.scanner;
    private static final Colorful cPrint = ResourceManager.cPrint;

    public static ObjWrapper checkAndGet(Class<?> formClazz) throws IllegalAccessException,
                                                                    InstantiationException,
                                                                    InvocationTargetException {
        Form form = formClazz.getAnnotation(Form.class);
        if (form == null)
            return ObjWrapper.failed();
        String exitCmd = form.dftExtCmd();
        Object instance = ClassUtils.newInstance(formClazz);
        Field[] fields = formClazz.getDeclaredFields();
        for (Field field : fields) {
            if (getAndSetProp(field, instance, exitCmd, null, false))
                break;
        }

        while (showProperties(instance, formClazz)) {
            modifyMode(instance, formClazz, exitCmd);
        }

        return ObjWrapper.success(instance);
    }


    private static boolean getAndSetProp(Field field, Object instance, String exitCmd,
                                      String customPrompt, boolean isModifyMode) {
        field.setAccessible(true);
        Prop prop = field.getAnnotation(Prop.class);
        if (prop == null)
            return false;
        boolean isRequired = prop.isRequired();
        String prompt = customPrompt == null ? field.getName() : customPrompt;
        if (customPrompt == null && !prop.prompt().equals("")) {
            prompt = prop.prompt();
        }
        prompt += isRequired ? "! " : "~ ";
        boolean isBreak = false;
        while (true) {
            cPrint.print(prompt);
            String input = scanner.nextLine().trim();
            final boolean flag = input.isEmpty() || input.equals(exitCmd);
            if (flag && (!isRequired || isModifyMode)) {
                isBreak = true;
                break;
            }
            if (isRequired && flag)
                continue;
            try {
                field.set(instance, ResolveFactory.resolveArgument(input, field.getType()));
                break;
            } catch (Exception e) {
                cPrint.println("属性值无效, msg:" + e.getMessage());
            }
        }
        return isBreak;
    }

    private static void modifyMode(Object instance, Class<?> form, String extCmd) throws IllegalAccessException {
        cPrint.println("进入编辑模式，无需改动则回车跳过，需要修改则输入新值覆盖旧值");
        StringBuilder sb = new StringBuilder();
        for (Field field : form.getDeclaredFields()) {
            sb.setLength(0);
            field.setAccessible(true);
            Object value = field.get(instance);
            String prompt = sb.append("[").append(field.getName()).append("\t")
                    .append(value).append("\t").append("] ").toString();
            if (getAndSetProp(field, instance, extCmd, prompt, true))
                break;
        }
    }

    private static boolean showProperties(Object instance, Class<?> form) throws IllegalAccessException {
        cPrint.println("以下是当前表单中的内容:");
        StringBuilder sb = new StringBuilder();
        for (Field field : form.getDeclaredFields()) {
            field.setAccessible(true);
            sb.setLength(0);
            Prop prop = field.getAnnotation(Prop.class);
            if (prop == null)
                continue;
            sb.append("[").append(field.getName()).append(":]")
                    .append(prop.isRequired() ? "! " : "~ ").append(field.get(instance));
            cPrint.println(sb);
        }
        cPrint.print("确认输入是否无误？Y(确认) N(需要改动，进入修改模式): ");
        String input = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        return !input.startsWith("y");
    }

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
