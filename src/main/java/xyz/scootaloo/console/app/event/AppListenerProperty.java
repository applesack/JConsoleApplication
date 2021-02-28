package xyz.scootaloo.console.app.event;

/**
 * 对监听器进行更细粒度的配置
 * @see EventType 事件描述
 * @author flutterdash@qq.com
 * @since 2021/2/10 21:57
 */
public final class AppListenerProperty {
    protected static final int DFT_PRIORITY = 5; // 默认优先级
    private final EventProperty eventProperty;   // 事件的属性

    public AppListenerProperty() {
        eventProperty = new EventProperty();
    }

    public AppListenerProperty onAppStarted() {
        return onAppStarted(DFT_PRIORITY);
    }

    public AppListenerProperty onAppStarted(int order) {
        this.eventProperty.onAppStarted.setPriority(order);
        return this;
    }

    public AppListenerProperty onInput() {
        return onInput(DFT_PRIORITY);
    }

    public AppListenerProperty onInput(int order) {
        this.eventProperty.onInput.setPriority(order);
        return this;
    }

    public AppListenerProperty onResolveInput() {
        return onResolveInput(DFT_PRIORITY);
    }

    public AppListenerProperty onResolveInput(int order) {
        this.eventProperty.onResolveInput.setPriority(order);
        return this;
    }

    public AppListenerProperty onInputResolved() {
        return onInputResolved(DFT_PRIORITY);
    }

    public AppListenerProperty onInputResolved(int order) {
        this.eventProperty.onInputResolved.setPriority(order);
        return this;
    }

    public AppListenerProperty onMessage() {
        return onMessage(DFT_PRIORITY);
    }

    public AppListenerProperty onMessage(int order) {
        this.eventProperty.onMessage.setPriority(order);
        return this;
    }

    protected EventProperty get() {
        return this.eventProperty;
    }

    /**
     *
     * @author flutterdash@qq.com
     * @since 2021/2/10 23:13
     */
    public static class EventProperty {
        protected final Property    onAppStarted = new Property(EventType.OnAppStarted);
        protected final Property         onInput = new Property(EventType.OnInput);
        protected final Property  onResolveInput = new Property(EventType.OnResolveInput);
        protected final Property onInputResolved = new Property(EventType.OnInputResolved);
        protected final Property       onMessage = new Property(EventType.OnMessage);

        public EventProperty() {
        }

        public Property get(EventType event) {
            switch (event) {
                case OnMessage: return onMessage;
                case OnInputResolved: return onInputResolved;
                case OnAppStarted: return onAppStarted;
                case OnInput: return onInput;
                case OnResolveInput: return onResolveInput;
            }
            return Property.DFT;
        }
    }

    public static class Property {
        private static final Property DFT = new Property(EventType.OnResolveInput);
        private boolean interestedIn = false; // 是否对此事件感兴趣
        private int priority = DFT_PRIORITY;  // 监听此事件时执行的优先级
        private final EventType event;          // 此事件对应的枚举类型

        public Property(EventType event) {
            this.event = event;
        }

        public int priority() {
            return this.priority;
        }

        public EventType getMoment() {
            return this.event;
        }

        public boolean isInterestedIn() {
            return this.interestedIn;
        }

        public void setPriority(int order) {
            this.interestedIn = true;
            this.priority = order;
        }

    }

}
