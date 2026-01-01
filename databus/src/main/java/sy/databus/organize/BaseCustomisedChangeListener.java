package sy.databus.organize;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class BaseCustomisedChangeListener<T> implements ChangeListener<T> {

    private final Object lock;

    private volatile boolean customised = true; // 标记当前监听者是否被console中的组件激活

    public void setCustomised(boolean flag) { // 使失活
        synchronized (lock) {
            customised = flag;
        }
    }

    protected BaseCustomisedChangeListener(Object lock) {
        this.lock = lock;
    }

    @Override
    public void changed(ObservableValue<? extends T> observableValue, T t, T t1) {
        synchronized (lock) {
            if (!customised) // 失活则直接退出
                return;
            syncChanged(observableValue, t, t1);
        }
    }

    protected abstract void syncChanged(ObservableValue<? extends T> observableValue, T t, T t1);
}
