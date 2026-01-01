package sy.common.concurrent.vector;

import com.sun.javafx.collections.ListListenerHelper;
import com.sun.javafx.collections.SourceAdapterChange;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * ps:
 * 1. SSyncObservableList 的各同步操作由 mutex 加锁，其mutex可由用户自行初始化，也可被用户get获取
 * 2. SSyncObservableList 的add remove等事件的监听者注册的方法也会被 mutex 同步。
 * 3. 若对象T没有重写hashCode()方法，则不可以用{@link #indexOf(Object)#lastIndexOf(Object)}方法， 否则索引对象时总是返回-1
 * */
public class SSyncObservableList<T> implements ObservableList<T> {

    private ListListenerHelper helper;
    private final ObservableList<T> backingList;
    private final ListChangeListener<T> listener;
    @Getter @Setter
    private Object mutex;

    public SSyncObservableList() {
        this.mutex = this;
        this.backingList = FXCollections.observableArrayList();
        this.listener = (var1x) -> {
            ListListenerHelper.fireValueChangedEvent(this.helper, new SourceAdapterChange(this, var1x));
        };
        this.backingList.addListener(new WeakListChangeListener(this.listener));
    }

    public SSyncObservableList(Object mutex) {
        this.mutex = mutex;
        this.backingList = FXCollections.observableArrayList();
        this.listener = (var1x) -> {
            ListListenerHelper.fireValueChangedEvent(this.helper, new SourceAdapterChange(this, var1x));
        };
        this.backingList.addListener(new WeakListChangeListener(this.listener));

    }

    public SSyncObservableList(ObservableList<T> var1, Object mutex) {
        this.backingList = var1;
        this.mutex = mutex;
        this.listener = (var1x) -> {
            ListListenerHelper.fireValueChangedEvent(this.helper, new SourceAdapterChange(this, var1x));
        };
        this.backingList.addListener(new WeakListChangeListener(this.listener));
    }

    public SSyncObservableList(ObservableList<T> var1) {
        this.mutex = this;
        this.backingList = var1;
        this.listener = (var1x) -> {
            ListListenerHelper.fireValueChangedEvent(this.helper, new SourceAdapterChange(this, var1x));
        };
        this.backingList.addListener(new WeakListChangeListener(this.listener));
    }


    public int size() {
        synchronized(this.mutex) {
            return this.backingList.size();
        }
    }

    public boolean isEmpty() {
        synchronized(this.mutex) {
            return this.backingList.isEmpty();
        }
    }

    public boolean contains(Object var1) {
        synchronized(this.mutex) {
            return this.backingList.contains(var1);
        }
    }

    public Iterator<T> iterator() {
        return this.backingList.iterator();
    }

    public Object[] toArray() {
        synchronized(this.mutex) {
            return this.backingList.toArray();
        }
    }

    public <T> T[] toArray(T[] var1) {
        synchronized(this.mutex) {
            return this.backingList.toArray(var1);
        }
    }

    public boolean add(T var1) {
        synchronized(this.mutex) {
            return this.backingList.add(var1);
        }
    }

    public boolean remove(Object var1) {
        synchronized(this.mutex) {
            return this.backingList.remove(var1);
        }
    }

    public boolean containsAll(Collection<?> var1) {
        synchronized(this.mutex) {
            return this.backingList.containsAll(var1);
        }
    }

    public boolean addAll(Collection<? extends T> var1) {
        synchronized(this.mutex) {
            return this.backingList.addAll(var1);
        }
    }

    public boolean addAll(int var1, Collection<? extends T> var2) {
        synchronized(this.mutex) {
            return this.backingList.addAll(var1, var2);
        }
    }

    public boolean removeAll(Collection<?> var1) {
        synchronized(this.mutex) {
            return this.backingList.removeAll(var1);
        }
    }

    public boolean retainAll(Collection<?> var1) {
        synchronized(this.mutex) {
            return this.backingList.retainAll(var1);
        }
    }

    public void clear() {
        synchronized(this.mutex) {
            this.backingList.clear();
        }
    }

    public T get(int var1) {
        synchronized(this.mutex) {
            return this.backingList.get(var1);
        }
    }

    public T set(int var1, T var2) {
        synchronized(this.mutex) {
            return this.backingList.set(var1, var2);
        }
    }

    public void add(int var1, T var2) {
        synchronized(this.mutex) {
            this.backingList.add(var1, var2);
        }
    }

    public T remove(int var1) {
        synchronized(this.mutex) {
            return this.backingList.remove(var1);
        }
    }

    public int indexOf(Object var1) {
        synchronized(this.mutex) {
            return this.backingList.indexOf(var1);
        }
    }

    public int lastIndexOf(Object var1) {
        synchronized(this.mutex) {
            return this.backingList.lastIndexOf(var1);
        }
    }

    public ListIterator<T> listIterator() {
        return this.backingList.listIterator();
    }

    public ListIterator<T> listIterator(int var1) {
        synchronized(this.mutex) {
            return this.backingList.listIterator(var1);
        }
    }

    public List<T> subList(int var1, int var2) {
        synchronized(this.mutex) {
            return new SSyncObservableList(FXCollections.observableList(this.backingList.subList(var1, var2)), this.mutex);
        }
    }

    public String toString() {
        synchronized(this.mutex) {
            return this.backingList.toString();
        }
    }

    public int hashCode() {
        synchronized(this.mutex) {
            return this.backingList.hashCode();
        }
    }

    public boolean equals(Object var1) {
        synchronized(this.mutex) {
            return this.backingList.equals(var1);
        }
    }

    public boolean addAll(T... var1) {
        synchronized(this.mutex) {
            return this.backingList.addAll(var1);
        }
    }

    public boolean setAll(T... var1) {
        synchronized(this.mutex) {
            return this.backingList.setAll(var1);
        }
    }

    public boolean removeAll(T... var1) {
        synchronized(this.mutex) {
            return this.backingList.removeAll(var1);
        }
    }

    public boolean retainAll(T... var1) {
        synchronized(this.mutex) {
            return this.backingList.retainAll(var1);
        }
    }

    public void remove(int var1, int var2) {
        synchronized(this.mutex) {
            this.backingList.remove(var1, var2);
        }
    }

    public boolean setAll(Collection<? extends T> var1) {
        synchronized(this.mutex) {
            return this.backingList.setAll(var1);
        }
    }

    public final void addListener(InvalidationListener var1) {
        synchronized(this.mutex) {
            this.helper = ListListenerHelper.addListener(this.helper, var1);
        }
    }

    public final void removeListener(InvalidationListener var1) {
        synchronized(this.mutex) {
            this.helper = ListListenerHelper.removeListener(this.helper, var1);
        }
    }

    public void addListener(ListChangeListener<? super T> var1) {
        synchronized(this.mutex) {
            this.helper = ListListenerHelper.addListener(this.helper, var1);
        }
    }

    public void removeListener(ListChangeListener<? super T> var1) {
        synchronized(this.mutex) {
            this.helper = ListListenerHelper.removeListener(this.helper, var1);
        }
    }

    @Getter
    private int movedSource = -1;
    @Getter
    private int movedTarget = -1;
    public void move(int startIndex, int targetIndex){
        synchronized (this.mutex) {
            T e = backingList.get(startIndex);
            movedSource = startIndex;
            backingList.remove(startIndex);
            movedTarget = targetIndex;
            backingList.add(targetIndex, e);
            movedSource = -1;
            movedTarget = -1;
        }
    }

    // 上移对象，返回上移的索引
    public int moveUp(T item) {
        synchronized (this.mutex) {
            int startPos = this.backingList.indexOf(item);
            if (startPos < 0) // 没找到
                return -1;
            int targetPos = startPos - 1;
            if (targetPos < this.backingList.size() && targetPos >= 0) {
                T e = this.backingList.get(startPos);
                movedSource = startPos;
                this.backingList.remove(startPos);
                movedTarget = targetPos;
                this.backingList.add(targetPos, e);
                movedSource = -1;
                movedTarget = -1;
                return targetPos;
            } else {
                return -1;
            }
        }
    }

    // 下移对象，返回下移的索引
    public int moveDown(T item) {
        synchronized (this.mutex) {
            int startPos = this.backingList.indexOf(item);
            if (startPos < 0) // 没找到
                return -1;
            int targetPos = startPos + 1;
            if (targetPos < this.backingList.size() && targetPos >= 0) {
                T e = this.backingList.get(startPos);
                movedSource = startPos;
                this.backingList.remove(startPos);
                movedTarget = targetPos;
                this.backingList.add(targetPos, e);
                movedSource = -1;
                movedTarget = -1;
                return targetPos;
            } else {
                return -1;
            }
        }
    }
}


