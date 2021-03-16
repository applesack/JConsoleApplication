package xyz.scootaloo.console.app.support;

import xyz.scootaloo.console.app.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 各种函数的接口描述
 * <pre>
 * 从无参到四参
 * 有返回值和无返回值
 * 全覆盖</pre>
 * @see InvokeProxy
 * @author flutterdash@qq.com
 * @since 2021/2/13 17:07
 */
public final class FunctionDesc {

    /** 描述有返回值，没有参数的方法 */
    @FunctionalInterface
    public interface Rtn0P<R> {
        R call() throws Exception;
    }

    /** 描述有返回值，有一个参数的方法 */
    @FunctionalInterface
    public interface Rtn1P<R, T> {
        R call(T param) throws Exception;
    }

    /** 描述有返回值，有两个参数的方法 */
    @FunctionalInterface
    public interface Rtn2P<R, T1, T2> {
        R call(T1 param1, T2 param2) throws Exception;
    }

    /** 描述有返回值，有三个参数的方法 */
    @FunctionalInterface
    public interface Rtn3P<R, T1, T2, T3> {
        R call(T1 param1, T2 param2, T3 param3) throws Exception;
    }

    /** 描述有返回值，有四个参数的方法 */
    @FunctionalInterface
    public interface Rtn4P<R, T1, T2, T3, T4> {
        R call(T1 param1, T2 param2, T3 param3, T4 param4) throws Exception;
    }

    /** 描述没有有返回值，没有参数的方法 */
    @FunctionalInterface
    public interface NonRtn0P {
        void call() throws Exception;
    }

    /** 描述没有返回值，有一个参数的方法 */
    @FunctionalInterface
    public interface NonRtn1P<T> {
        void call(T param) throws Exception;
    }

    /** 描述没有返回值，有两个参数的方法 */
    @FunctionalInterface
    public interface NonRtn2P<T1, T2> {
        void call(T1 param1, T2 Param2) throws Exception;
    }

    /** 描述没有返回值，有三个参数的方法 */
    @FunctionalInterface
    public interface NonRtn3P<T1, T2, T3> {
        void call(T1 param1, T2 Param2, T3 param3) throws Exception;
    }

    /** 描述没有返回值，有四个参数的方法 */
    @FunctionalInterface
    public interface NonRtn4P<T1, T2, T3, T4> {
        void call(T1 param1, T2 Param2, T3 param3, T4 param4) throws Exception;
    }

    /**
     * *方法包装*
     * <pre>
     * 一下的所有类都可以分为两种类型:
     *      1. 有返回值的方法包装，且参数少于等于4个
     *      2. 没有返回值的方法包装，参数少于等于4个
     * 不推荐直接实例化这些类，使用入口请 参考 InvokeProxy {@link InvokeProxy}
     * 公共的方法有:
     *      addHandle 提供异常处理器，当被代理的方法被调用后处理器被触发
     * 当方法有返回值，可以还可以额外实用这些方法
     *      setDefault 在被代理的方法抛出异常时，自动应用默认值
     *      setSupplier 在被代理的方法抛出异常时，自动应用默认值(通过lambda表达式得到)
     *      getOptional 返回Optional对象，其中包含方法调用结果，假如方法抛出异常，则其中是默认值，假如没有默认值，则得到Optional.empty()
     * </pre>
     */
    public static abstract class NonRtnMethod {
        /** 一个异常处理器队列 */
        protected final List<ExceptionHandler<? extends Exception>> handlers = new ArrayList<>();

        /**
         * 公用的添加异常处理器方法
         * @param handle 异常处理器
         */
        protected void xAddHandle(Consumer<Exception> handle) {
            if (handle != null)
                handlers.add(ExceptionHandler.getDefault(handle));
        }

        protected <Ex extends Exception> void xAddHandle(Class<Ex> exType, Consumer<Ex> handle) {
            if (handle != null)
                handlers.add(new ExceptionHandler<>(exType, handle));
        }

        /**
         * 用于处理无返回值的方法所抛出的异常
         * @param e 被捕获到的异常
         */
        protected void xHandle(Exception e) {
            // simply
            handlers.forEach(handler -> handler.handle(e));
        }

    }

    /**
     * 一般有返回值的方法
     * @param <R> 方法返回值
     */
    public abstract static class CommonMethod<R> extends NonRtnMethod {
        /** 一个默认值提供者 */
        protected Supplier<R> supplier;

        /**
         * 指定一个默认值，当被调用的方法抛出异常时应用这个默认值
         * @param value 默认值
         */
        protected void xSetDefault(R value) {
            xSetSupplier(() -> value);
        }

        /**
         * @param supplier 默认值提供者
         */
        protected void xSetSupplier(Supplier<R> supplier) {
            if (supplier != null)
                this.supplier = supplier;
        }

        /**
         * @param e 捕获的异常
         * @return 默认值，假如没有提供默认值，则返回null
         */
        protected <M extends CommonMethod<R>> R rHandle(M wrapper, Exception e) {
            super.xHandle(e);
            return wrapper.supplier != null ? wrapper.supplier.get() : null;
        }

    }

    public static class NonRtn0pWrapper extends NonRtnMethod {
        private final NonRtn0P method;
        public NonRtn0pWrapper(NonRtn0P method) {
            this.method = method;
        }

        public NonRtn0pWrapper addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public NonRtn0pWrapper addHandle(Class<Exception> exType, Consumer<Exception> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public void call() {
            try {
                method.call();
            } catch (Exception e) {
                super.xHandle(e);
            }
        }
    }

    public static class NonRtn1pWrapper<T> extends NonRtnMethod {
        private final NonRtn1P<T> method;
        public NonRtn1pWrapper(NonRtn1P<T> method) {
            this.method = method;
        }

        public NonRtn1pWrapper<T> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> NonRtn1pWrapper<T> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public void call(T param) {
            try {
                method.call(param);
            } catch (Exception e) {
                super.xHandle(e);
            }
        }
    }

    public static class NonRtn2pWrapper<T1, T2> extends NonRtnMethod {
        private final NonRtn2P<T1, T2> method;
        public NonRtn2pWrapper(NonRtn2P<T1, T2> method) {
            this.method = method;
        }

        public NonRtn2pWrapper<T1, T2> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> NonRtn2pWrapper<T1, T2> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public void call(T1 param1, T2 param2) {
            try {
                method.call(param1, param2);
            } catch (Exception e) {
                xHandle(e);
            }
        }
    }

    public static class NonRtn3pWrapper<T1, T2, T3> extends NonRtnMethod {
        private final NonRtn3P<T1, T2, T3> method;
        public NonRtn3pWrapper(NonRtn3P<T1, T2, T3> method) {
            this.method = method;
        }

        public NonRtn3pWrapper<T1, T2, T3> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> NonRtn3pWrapper<T1, T2, T3> addHandle(Class<Ex> exType,
                                                                            Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public void call(T1 param1, T2 param2, T3 param3) {
            try {
                method.call(param1, param2, param3);
            } catch (Exception e) {
                xHandle(e);
            }
        }
    }

    public static class NonRtn4pWrapper<T1, T2, T3, T4> extends NonRtnMethod {
        private final NonRtn4P<T1, T2, T3, T4> method;
        public NonRtn4pWrapper(NonRtn4P<T1, T2, T3, T4> method) {
            this.method = method;
        }

        public NonRtn4pWrapper<T1, T2, T3, T4> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> NonRtn4pWrapper<T1, T2, T3, T4> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public void call(T1 param1, T2 param2, T3 param3, T4 param4) {
            try {
                method.call(param1, param2, param3, param4);
            } catch (Exception e) {
                xHandle(e);
            }
        }
    }

    public static class Rtn0pWrapper<R> extends CommonMethod<R> {
        private final Rtn0P<R> method;
        public Rtn0pWrapper(Rtn0P<R> method) {
            this.method = method;
        }

        public Rtn0pWrapper<R> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> Rtn0pWrapper<R> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public Rtn0pWrapper<R> setDefault(R value) {
            super.xSetDefault(value);
            return this;
        }

        public Rtn0pWrapper<R> setSupplier(Supplier<R> supplier) {
            super.xSetSupplier(supplier);
            return this;
        }

        public Optional<R> getOptional() {
            return Optional.ofNullable(call());
        }

        public R call() {
            try {
                return method.call();
            } catch (Exception e) {
                return super.rHandle(this, e);
            }
        }
    }

    public static class Rtn1pWrapper<R, T> extends CommonMethod<R> {
        private final Rtn1P<R, T> method;
        public Rtn1pWrapper(Rtn1P<R, T> method) {
            this.method = method;
        }

        public Rtn1pWrapper<R, T> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> Rtn1pWrapper<R, T> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public Rtn1pWrapper<R, T> setDefault(R value) {
            super.xSetDefault(value);
            return this;
        }

        public Rtn1pWrapper<R, T> setSupplier(Supplier<R> supplier) {
            super.xSetSupplier(supplier);
            return this;
        }

        public Optional<R> getOptional(T param) {
            return Optional.ofNullable(call(param));
        }

        public R call(T param) {
            try {
                return method.call(param);
            } catch (Exception e) {
                return super.rHandle(this, e);
            }
        }
    }

    public static class Rtn2pWrapper<R, T1, T2> extends CommonMethod<R> {
        private final Rtn2P<R, T1, T2> method;
        public Rtn2pWrapper(Rtn2P<R, T1, T2> method) {
            this.method = method;
        }

        public Rtn2pWrapper<R, T1, T2> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> Rtn2pWrapper<R, T1, T2> addHandle(Class<Ex> exType, Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public Rtn2pWrapper<R, T1, T2> setDefault(R value) {
            super.xSetDefault(value);
            return this;
        }

        public Rtn2pWrapper<R, T1, T2> setSupplier(Supplier<R> supplier) {
            super.xSetSupplier(supplier);
            return this;
        }

        public Optional<R> getOptional(T1 param1, T2 param2) {
            return Optional.ofNullable(call(param1, param2));
        }

        public R call(T1 param1, T2 param2) {
            try {
                return method.call(param1, param2);
            } catch (Exception e) {
                return super.rHandle(this, e);
            }
        }
    }

    public static class Rtn3pWrapper<R, T1, T2, T3> extends CommonMethod<R> {
        private final Rtn3P<R, T1, T2, T3> method;
        public Rtn3pWrapper(Rtn3P<R, T1, T2, T3> method) {
            this.method = method;
        }

        public Rtn3pWrapper<R, T1, T2, T3> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> Rtn3pWrapper<R, T1, T2, T3> addHandle(Class<Ex> exType,
                                                                            Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public Rtn3pWrapper<R, T1, T2, T3> setDefault(R value) {
            super.xSetDefault(value);
            return this;
        }

        public Rtn3pWrapper<R, T1, T2, T3> setSupplier(Supplier<R> supplier) {
            super.xSetSupplier(supplier);
            return this;
        }

        public Optional<R> getOptional(T1 param1, T2 param2, T3 param3) {
            return Optional.ofNullable(call(param1, param2, param3));
        }

        public R call(T1 param1, T2 param2, T3 param3) {
            try {
                return method.call(param1, param2, param3);
            } catch (Exception e) {
                return super.rHandle(this, e);
            }
        }
    }

    public static class Rtn4pWrapper<R, T1, T2, T3, T4> extends CommonMethod<R> {
        private final Rtn4P<R, T1, T2, T3, T4> method;
        public Rtn4pWrapper(Rtn4P<R, T1, T2, T3, T4> method) {
            this.method = method;
        }

        public Rtn4pWrapper<R, T1, T2, T3, T4> addHandle(Consumer<Exception> handle) {
            super.xAddHandle(handle);
            return this;
        }

        public <Ex extends Exception> Rtn4pWrapper<R, T1, T2, T3, T4> addHandle(Class<Ex> exType,
                                                                                Consumer<Ex> handle) {
            super.xAddHandle(exType, handle);
            return this;
        }

        public Rtn4pWrapper<R, T1, T2, T3, T4> setDefault(R value) {
            super.xSetDefault(value);
            return this;
        }

        public Rtn4pWrapper<R, T1, T2, T3, T4> setSupplier(Supplier<R> supplier) {
            super.xSetSupplier(supplier);
            return this;
        }

        public Optional<R> getOptional(T1 param1, T2 param2, T3 param3, T4 param4) {
            return Optional.ofNullable(call(param1, param2, param3, param4));
        }

        public R call(T1 param1, T2 param2, T3 param3, T4 param4) {
            try {
                return method.call(param1, param2, param3, param4);
            } catch (Exception e) {
                return super.rHandle(this, e);
            }
        }
    }

    /** 异常处理模型 */
    public static class ExceptionHandler<Ex extends Exception> {
        private final Class<Ex> type;
        private final Consumer<Ex> consumer;

        public ExceptionHandler(Class<Ex> type, Consumer<Ex> consumer) {
            this.type = type;
            this.consumer = consumer;
        }

        @SuppressWarnings({ "unchecked", "hiding" })
        public void handle(Exception e) {
            if (ClassUtils.isExtendForm(e, type)) {
                consumer.accept((Ex) e);
            }
        }

        public static ExceptionHandler<Exception> getDefault(Consumer<Exception> consumer) {
            return new ExceptionHandler<>(Exception.class, consumer);
        }

    }

}
