package sy.databus.process.fsm;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import lombok.Getter;

public class SyncObservableState extends SimpleObjectProperty<PopularState> {

    @Getter
    private Object stateLocker = new Object();

    public SyncObservableState() {
        super(PopularState.RUNNABLE);
    }

    public SyncObservableState(PopularState state) {
        super(state);
    }

    @Override
    public void set(PopularState state) {
        synchronized (stateLocker) {
            super.set(state);
        }
    }

    @Override
    public PopularState get() {
        synchronized (stateLocker) {
            return super.get();
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        synchronized (stateLocker) {
            super.addListener(invalidationListener);
        }
    }

    @Override
    public void addListener(ChangeListener<? super PopularState> changeListener) {
        synchronized (stateLocker) {
            super.addListener(changeListener);
        }
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        synchronized (stateLocker) {
            super.removeListener(invalidationListener);
        }
    }

    @Override
    public void removeListener(ChangeListener<? super PopularState> changeListener) {
        synchronized (stateLocker) {
            super.removeListener(changeListener);
        }
    }
}
