package sy.databus.process.fsm.producer;

import lombok.Getter;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static sy.databus.process.fsm.producer.State.ActionType.*;

@Log4j2
public class ProducerCommonCondition {

    @Getter
    private volatile State current;

    @Getter
    private final Lock conLock = new ReentrantLock();

    private State.ActionType conLockHolder = null;

    @Getter @Setter
    private Future<?> future;
    public Object getFutureResult() throws ExecutionException, InterruptedException {
        if (future == null) {
            log.warn("Current condition has no future obj!");
            return null;
        }
        return future.get();
    }

    public ProducerCommonCondition(RUNNABLE runnable,
                                   RUNNING running,
                                   SUSPENDED suspended,
                                   TERMINATED terminated) {
        this._RUNNABLE = runnable;
        this._RUNNING = running;
        this._SUSPENDED = suspended;
        this._TERMINATED = terminated;

        // 初次创建后默认是_RUNNABLE状态
        this.current = runnable;
    }

    public ProducerCommonCondition(Class<? extends State> initialState,
                                   RUNNABLE runnable,
                                   RUNNING running,
                                   SUSPENDED suspended,
                                   TERMINATED terminated) {
        this._RUNNABLE = runnable;
        this._RUNNING = running;
        this._SUSPENDED = suspended;
        this._TERMINATED = terminated;

        // 初次创建后默认是_RUNNABLE状态
        if (RUNNABLE.class == initialState) {
            this.current = runnable;
        } else if (RUNNING.class == initialState) {
            this.current = running;
        } else if (SUSPENDED.class == initialState) {
            this.current = suspended;
        } else if (TERMINATED.class == initialState) {
            this.current = terminated;
        } else {
            throw new RuntimeException("unkonwn initial state!");
        }
    }

    protected final State _RUNNABLE;
    protected final State _RUNNING;
    protected final State _SUSPENDED;
    protected final State _TERMINATED;

    // -> RUNNING
    public void open() {
        if (!current.containsAction(OPEN))
            return;
        if (conLock.tryLock()) {
            conLockHolder = OPEN;
            try {
                // TODO 状态机在调用toOpen方法时应当设置future，考虑如何规范
                toOpen();
            } catch (Exception e) {
                log.error(e);
            } finally {
                current = _RUNNING;
                conLock.unlock();
            }
        }
    }

    private void toOpen() {
        current.doAction(OPEN);
    }

    // -> SUSPENDED
    public void park() {
        if (!current.containsAction(PARK))
            return;
        if (conLock.tryLock()) {
            conLockHolder = PARK;
            try {
                toPark();
            } catch (Exception e) {
                log.error(e);
            } finally {
                current = _SUSPENDED;
                conLock.unlock();
            }
        }
    }

    private void toPark() {
        current.doAction(PARK);
    }

    // -> RUNNABLE
    @SneakyThrows
    public void close() {
        if (!current.containsAction(CLOSE))
            return;
        if (conLock.tryLock()) {
            conLockHolder = CLOSE;
            try {
                current.doAction(CLOSE);
                // TODO toClose()是同步关闭，其中应当调用future.get()，考虑规范到这里
                getFutureResult();/** 同步状态，确保关闭*/
            } catch (Exception e) {
                log.error(e.getMessage());
            } finally {
                future = null;
                current = _RUNNABLE;
                conLock.unlock();
            }
        }
    }

    public void selfClosing() {
        boolean locked = false; // 是否抢到锁
        try {
            while (true) {
                locked = conLock.tryLock();
                if (locked)
                    break;
                if (conLockHolder == CLOSE || conLockHolder == SHUTDOWN)
                    return;
            }
            if (conLockHolder == PARK) {
                toOpen();
            }
            current = _RUNNABLE;
        } catch (Exception e) {
            log.error(e);
        } finally {
            if (locked)
                conLock.unlock();
        }
    }

    // -> TERMINATED
    @SneakyThrows
    public void shutdown() {
        if (!current.containsAction(SHUTDOWN))
            return;
        // TODO toShutdown()是异步关闭，其中应将future赋给processor的closingFuture
        if (conLock.tryLock()) {
            conLockHolder = SHUTDOWN;
            try {
                current.doAction(SHUTDOWN);
                getFutureResult();/** 同步状态，确保关闭*/
            } catch (Exception e) {
                log.error(e);
            } finally {
                current = _TERMINATED;
                future = null;
                conLock.unlock();
            }
        }
    }

    public boolean isRunnable() {
        return current == _RUNNABLE;
    }

    public boolean isRunning() {
        return current == _RUNNING;
    }

    public boolean isSuspended() {
        return current == _SUSPENDED;
    }

    public boolean isTerminated() {
        return current == _TERMINATED;
    }

    public void switchToRunnable() {
        conLock.lock();
            current = _RUNNABLE;
        conLock.unlock();
    }

    public void switchToRunning() {
        conLock.lock();
            current = _RUNNING;
        conLock.unlock();
    }

    public void switchToSuspended() {
        conLock.lock();
            current = _SUSPENDED;
        conLock.unlock();
    }

    public void switchToTerminated() {
        conLock.lock();
            current = _TERMINATED;
        conLock.unlock();
    }
    
}
