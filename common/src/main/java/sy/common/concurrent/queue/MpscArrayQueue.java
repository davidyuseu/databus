package sy.common.concurrent.queue;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ps：
 * 1. 该任务队列仅适用于多生产者，单消费者模型；
 * 2. 该任务队列可以实现生产者无锁的原子性添加任务
 * 3. 该任务队列为无界队列，可以设置容量的初始化大小，任务数量超出后会自动扩容
 * */
public class MpscArrayQueue<E> extends MpscUnboundedArrayQueue<E> implements BlockingQueue<E> {

    private final Lock lock = new ReentrantLock();
    private final Condition singleConsumerWaitSet = lock.newCondition();

    public MpscArrayQueue() {
        super(32768);
    }

    public MpscArrayQueue(int capacity) {
        super(capacity);
    }

    @Override
    public E take() throws InterruptedException {
        E res;
        if((res = poll()) == null) {
            lock.lock();
            try {
                while ((res = poll()) == null) {
                    singleConsumerWaitSet.await();
                }
            } finally {
                lock.unlock();
            }
        }
        return res;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E res;
        if((res = poll()) == null) {
            lock.lock();
            try {
                long nanos = unit.toNanos(timeout);
                while((res = poll()) == null) {
                    try {
                        if (nanos <= 0) {
                            return null;
                        }
                        nanos = singleConsumerWaitSet.awaitNanos(nanos);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return res;
            } finally {
                lock.unlock();
            }
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
     * 非阻塞地添加元素
     */
    @Override
    public boolean offer(E e) {
        if(super.offer(e)) {
            signalAllWhenBlocking();
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

    /**
     * 通知消费者，队列已有数据可消费
     */
    public void signalAllWhenBlocking(){
        lock.lock();
        try {
            singleConsumerWaitSet.signalAll();
        } finally {
            lock.unlock();
        }
    }
    
}
