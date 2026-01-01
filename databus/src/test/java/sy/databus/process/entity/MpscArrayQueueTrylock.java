package sy.databus.process.entity;

import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueueUtil;
import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class MpscArrayQueueTrylock <E> extends MpscUnboundedArrayQueue<E> implements BlockingQueue<E> {

    // 消费锁
    private final Lock lock = new ReentrantLock();
    private final Condition singleConsumerWaitSet = lock.newCondition();

    public MpscArrayQueueTrylock() {
        super(32768);
    }

    public MpscArrayQueueTrylock(int capacity) {
        super(capacity);
    }

    // 阻塞获取
    @Override
    public E take() throws InterruptedException {
        E res;
        if((res = poll()) == null) {
            lock.lock();
            try {
                while ((res = poll()) == null) {
                    singleConsumerWaitSet.await(1, TimeUnit.MILLISECONDS);
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
     * @Description: 非阻塞地添加元素
     * @Date  2022-01-21 04-21-57
     * @Param:
     * @return:
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
     * @Description: 通知消费者，队列已有数据可消费
     * @Date  2022-01-21 04-21-10
     * @Param:
     * @return:
     */
    public void signalAllWhenBlocking(){
        if (lock.tryLock()) {
            try {
                singleConsumerWaitSet.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

/*    public void signalAllWhenBlocking() {
            singleConsumerWaitSet.signalAll();
    }*/

    /**
     * @Description: 非阻塞地添加元素的同时关注任务队列的大小是否达到数量限制 {@value limit}
     * @Date  2022-01-21 04-14-14
     * @Param:
     * @return: int
     *  为0时，插入元素操作(offer)失败；
     *  为1时，插入元素成功；
     *  为-1时，当前队列大小已达数量限制 {@value limit}
     */
    /**任务最大数目限制，
     * ps: 不是精确限制，主要旨在反映任务处理队列的情况 {@link #offerAndCheckPending(Object)}
     * */
/*    public int offerAndCheckPending(E e){
        if((size()) >= limit){
            signalAllWhenBlocking();
            return -1;
        }else{
            if(offer(e)) {
                signalAllWhenBlocking();
                return 1;
            }else{
                return 0;
            }
        }
    }*/
}
