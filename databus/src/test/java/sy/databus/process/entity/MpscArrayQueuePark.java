package sy.databus.process.entity;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;
import lombok.Setter;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

public class MpscArrayQueuePark<E> extends MpscUnboundedArrayQueue<E> implements BlockingQueue<E> {

    @Setter
    private Thread customer;
    private final AtomicBoolean signalNeeded = new AtomicBoolean(false);

    public MpscArrayQueuePark() {
        super(32768);
    }

    public MpscArrayQueuePark(int capacity) {
        super(capacity);
    }

    // 阻塞获取
    @Override
    public E take() throws InterruptedException {
        E res;
        while ((res = poll()) == null) {
            signalNeeded.getAndSet(true);
            if ((res = poll()) == null )
                LockSupport.park();
            else
                return res;
        }
        return res;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E res;
        if((res = poll()) == null) {
            long nanos = unit.toNanos(timeout);
            LockSupport.parkNanos(nanos);
            if ((res = poll()) == null)
                return null;

        }
        return res;
    }

    @Override
    public int remainingCapacity() {
        return this.size();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return MessagePassingQueueUtil.drain(this, e -> {
            c.add(e);
        }, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        return MessagePassingQueueUtil.drain(this, e -> {
            c.add(e);
        }, maxElements);
    }

    /**
     * @Description: 非阻塞地添加元素
     * @Date  2022-01-21 04-21-57
     * @Param:
     * @return:
     */
    @Override
    public boolean offer(E e) {
        if(super.offer(e)) {
            if (signalNeeded.getAndSet(false)){
                LockSupport.unpark(customer);
            }
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        if (!offer(e))
            throw new IllegalStateException("Queue full");
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        int count = 0;
        while(!offer(e)) {
            LockSupport.parkNanos(100l);
            count++;
            if(100l * count > nanos)
                return false;
        }
        return true;
    }
}
