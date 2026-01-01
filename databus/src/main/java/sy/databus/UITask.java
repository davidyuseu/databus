package sy.databus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class UITask {

    protected Runnable task;

    private ScheduledFuture<?> future = null;
    public boolean isStopped() {
        return future == null
                || future.isCancelled();
    }

    protected UITask() {}

    public UITask(Runnable task) {
        this.task = task;
    }

    public void start(ScheduledThreadPoolExecutor executor,
                      long initialDelay,
                      long period,
                      TimeUnit unit) {
        future = executor.scheduleAtFixedRate(task,
                initialDelay,
                period, unit);
    }

    public void cancel(boolean mayInterruptIfRunning) {
        future.cancel(mayInterruptIfRunning);
    }

}
