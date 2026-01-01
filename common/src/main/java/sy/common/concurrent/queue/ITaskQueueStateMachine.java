package sy.common.concurrent.queue;

import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

public interface ITaskQueueStateMachine {
    boolean isShutdown();

    boolean isOpening();

    boolean isSuspended();

    Future<?> open();

    Future<?> suspend();

    Future<?> shutdown();

    Future<?> shutdown(long var1, long var3, TimeUnit var5);
}
