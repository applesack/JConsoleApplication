package xyz.scootaloo.console.app.util;

import xyz.scootaloo.console.app.config.ConsoleConfig;
import xyz.scootaloo.console.app.listener.AppListenerAdapter;
import xyz.scootaloo.console.app.listener.AppListenerProperty;
import xyz.scootaloo.console.app.util.FunctionDescribe.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 传递或者调用会抛出异常的方法
 * @author flutterdash@qq.com
 * @since 2021/2/12 0:21
 */
public final class InvokeProxy implements AppListenerAdapter {
    private static ConsoleConfig conf;
    private static final InvokeProxy INSTANCE = new InvokeProxy();

    public static <R> MethodCallWrapper<R> fun(FunctionDescribe.Rtn0P<R> rtn0P) {
        return new MethodCallWrapper<>((Object ... args) -> rtn0P.call());
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <R, T> MethodCallWrapper<R> fun(FunctionDescribe.Rtn1P<R, T> rtn1P) {
        return new MethodCallWrapper<>((Object ... args) -> rtn1P.call((T) args[0]));
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <R, T1, T2> MethodCallWrapper<R> fun(Rtn2P<R, T1, T2> rtn2P) {
        return new MethodCallWrapper<>((Object ... args) ->
                rtn2P.call((T1) args[0], (T2) args[1]));
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <R, T1, T2, T3> MethodCallWrapper<R> fun(FunctionDescribe.Rtn3P<R, T1, T2, T3> rtn3P) {
        return new MethodCallWrapper<>((Object ... args) ->
                rtn3P.call((T1) args[0], (T2) args[1], (T3) args[2]));
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <R, T1, T2, T3, T4> MethodCallWrapper<R> fun(FunctionDescribe.Rtn4P<R, T1, T2, T3, T4> rtn4P) {
        return new MethodCallWrapper<>((Object ... args) ->
                rtn4P.call((T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3]));
    }

    public static MethodCallWrapper<Non> fun(NonRtn0P nonRtn0P) {
        return new MethodCallWrapper<>((Object ... args) -> {
            nonRtn0P.call();
            return Non.singleton;
        });
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T> MethodCallWrapper<Non> fun(NonRtn1P<T> nonRtn1P) {
        return new MethodCallWrapper<>((Object ... args) -> {
            nonRtn1P.call((T) args[0]);
            return Non.singleton;
        });
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T1, T2> MethodCallWrapper<Non> fun(NonRtn2P<T1, T2> nonRtn2P) {
        return new MethodCallWrapper<>((Object ... args) -> {
            nonRtn2P.call((T1) args[0], (T2) args[1]);
            return Non.singleton;
        });
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T1, T2, T3> MethodCallWrapper<Non> fun(NonRtn3P<T1, T2, T3> nonRtn3P) {
        return new MethodCallWrapper<>((Object ... args) -> {
            nonRtn3P.call((T1) args[0], (T2) args[1], (T3) args[2]);
            return Non.singleton;
        });
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public static <T1, T2, T3, T4> MethodCallWrapper<Non> fun(NonRtn4P<T1, T2, T3, T4> nonRtn4P) {
        return new MethodCallWrapper<>((Object ... args) -> {
            nonRtn4P.call((T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3]);
            return Non.singleton;
        });
    }

    public static InvokeProxy getInstance() {
        return INSTANCE;
    }

    // 监听器

    @Override
    public String getName() {
        return "invokeProxy";
    }

    @Override
    public void config(AppListenerProperty interested) {
        interested.onAppStarted(Integer.MIN_VALUE);
    }

    @Override
    public void onAppStarted(ConsoleConfig config) {
        conf = config;
    }

    @Override
    public String info() {
        return "仅用于在系统启动时获取配置";
    }

    // 得到结果

    public static class MethodCallWrapper<T> {
        private final List<ExceptionHandle> handles = new ArrayList<>();
        private final CommonMethod<T> method;
        private Supplier<T> supplier;

        private MethodCallWrapper(CommonMethod<T> method) {
            this.method = method;
        }

        public Optional<T> getOptional(Object ... args) {
            return Optional.ofNullable(call(args));
        }

        public T call(Object ... args) {
            try {
                return method.call(args);
            } catch (Exception e) {
                if (conf != null && conf.isPrintStackTraceOnException())
                    e.printStackTrace();
               handles.forEach(handle -> {
                   if (ClassUtils.isExtendForm(e, handle.type))
                       handle.consumer.accept(e);
               });
               if (supplier != null)
                   return supplier.get();
               return null;
            }
        }

        public MethodCallWrapper<T> addHandle(Consumer<Exception> handle) {
            if (handle != null)
                this.handles.add(new ExceptionHandle(handle));
            return this;
        }

        public MethodCallWrapper<T> addHandle(Class<Exception> type, Consumer<Exception> handle) {
            if (type != null && handle != null)
                this.handles.add(new ExceptionHandle(type, handle));
            return this;
        }

        public MethodCallWrapper<T> setElse(Supplier<T> supplier) {
            if (supplier != null)
                this.supplier = supplier;
            return this;
        }

    }

    // 无结果，占位

    private static final class Non {
        private static final Non singleton = new Non();
        private Non() {
        }
    }

    // 异常处理器模型

    private static class ExceptionHandle {
        private final Class<Exception> type;
        private final Consumer<Exception> consumer;

        public ExceptionHandle(Consumer<Exception> consumer) {
            this(Exception.class, consumer);
        }

        public ExceptionHandle(Class<Exception> type, Consumer<Exception> consumer) {
            this.type = type;
            this.consumer = consumer;
        }

    }

    //-------------------------------Model------------------------------------------

    @FunctionalInterface
    protected interface CommonMethod<T> {

        T call(Object ... args) throws Exception;

    }

}
