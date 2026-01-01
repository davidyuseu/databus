package sy.common.concurrent.queue;

import com.lmax.disruptor.*;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * MPSC (Multi-Producer / Single-Consumer) BlockingQueue implemented with LMAX Disruptor RingBuffer.
 */
public class MpscDisruptorQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    private static final long PARK_NANOS = 100L;

    private static final class Slot<T> {
        T value;
    }

    private final int bufferSize;
    private final RingBuffer<Slot<E>> ringBuffer;

    private final Sequence consumerSequence = new Sequence(Sequence.INITIAL_VALUE);

    /** 用于 take() 阻塞等待 */
    private final SequenceBarrier barrier;

    public MpscDisruptorQueue() {
        this(32768);
    }

    public MpscDisruptorQueue(int capacity) {
        this(capacity, new BlockingWaitStrategy());
    }

    public MpscDisruptorQueue(int capacity, WaitStrategy waitStrategy) {
        this.bufferSize = normalizeToPowerOfTwo(capacity);
        this.ringBuffer = RingBuffer.createMultiProducer(() -> new Slot<>(), this.bufferSize, waitStrategy);
        this.ringBuffer.addGatingSequences(consumerSequence);
        this.barrier = this.ringBuffer.newBarrier();
    }

    public int capacity() {
        return bufferSize;
    }

    /**
     * 生产侧：不丢数据策略（背压）。
     * 当 ring buffer 满时，不返回 false，而是等待直到可写。
     */
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e, "e");

        for (;;) {
            try {
                final long seq = ringBuffer.tryNext();
                try {
                    ringBuffer.get(seq).value = e;
                } finally {
                    ringBuffer.publish(seq);
                }
                return true;
            } catch (InsufficientCapacityException ice) {
                LockSupport.parkNanos(this, PARK_NANOS);
                if (Thread.interrupted()) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e, "e");
        while (true) {
            if (Thread.interrupted()) throw new InterruptedException();
            try {
                final long seq = ringBuffer.tryNext();
                try {
                    ringBuffer.get(seq).value = e;
                } finally {
                    ringBuffer.publish(seq);
                }
                return;
            } catch (InsufficientCapacityException ice) {
                LockSupport.parkNanos(this, PARK_NANOS);
            }
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e, "e");
        long nanos = unit.toNanos(timeout);
        final long deadline = System.nanoTime() + nanos;

        while (true) {
            if (Thread.interrupted()) throw new InterruptedException();

            try {
                final long seq = ringBuffer.tryNext();
                try {
                    ringBuffer.get(seq).value = e;
                } finally {
                    ringBuffer.publish(seq);
                }
                return true;
            } catch (InsufficientCapacityException ice) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    return false;
                }
                LockSupport.parkNanos(this, Math.min(nanos, PARK_NANOS));
            }
        }
    }

    /** 消费侧：非阻塞 poll */
    @Override
    public E poll() {
        final long next = consumerSequence.get() + 1L;
        if (!ringBuffer.isAvailable(next)) {
            return null;
        }

        final Slot<E> slot = ringBuffer.get(next);
        final E val = slot.value;
        slot.value = null;               // help GC / avoid retaining
        consumerSequence.set(next);      // advance gating sequence
        return val;
    }

    /** 消费侧：阻塞 take（使用 SequenceBarrier + WaitStrategy） */
    @Override
    public E take() throws InterruptedException {
        E v = poll();
        if (v != null) {
            return v;
        }

        final long next = consumerSequence.get() + 1L;
        for (;;) {
            if (Thread.interrupted()) throw new InterruptedException();

            try {
                final long available = barrier.waitFor(next);
                if (available >= next) {
                    final Slot<E> slot = ringBuffer.get(next);
                    v = slot.value;
                    slot.value = null;
                    consumerSequence.set(next);
                    return v;
                }
            } catch (com.lmax.disruptor.TimeoutException ignore) {
            } catch (AlertException ae) {
                InterruptedException ie = new InterruptedException("Disruptor barrier alerted");
                ie.initCause(ae);
                throw ie;
            }
        }
    }

    /**
     * 消费侧：带超时 poll
     * 说明：为保持实现简单，不使用 barrier 的 timeout wait（需要特定 WaitStrategy），这里采用 park 轮询。
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        if (nanos <= 0L) {
            return poll();
        }

        final long deadline = System.nanoTime() + nanos;
        E v;
        while ((v = poll()) == null) {
            if (Thread.interrupted()) throw new InterruptedException();
            nanos = deadline - System.nanoTime();
            if (nanos <= 0L) return null;
            LockSupport.parkNanos(this, Math.min(nanos, PARK_NANOS));
        }
        return v;
    }

    @Override
    public E peek() {
        final long next = consumerSequence.get() + 1L;
        if (!ringBuffer.isAvailable(next)) {
            return null;
        }
        return ringBuffer.get(next).value;
    }

    @Override
    public int size() {
        final long cursor = ringBuffer.getCursor();
        final long consumed = consumerSequence.get();
        final long size = cursor - consumed;
        if (size <= 0L) return 0;
        return size > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) size;
    }

    @Override
    public int remainingCapacity() {
        final long remaining = ringBuffer.remainingCapacity();
        if (remaining <= 0L) return 0;
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c, "c");
        if (c == this) throw new IllegalArgumentException("Cannot drainTo self");
        if (maxElements <= 0) return 0;

        int n = 0;
        while (n < maxElements) {
            E v = poll();
            if (v == null) break;
            c.add(v);
            n++;
        }
        return n;
    }

    @Override
    public Iterator<E> iterator() {
        final long start = consumerSequence.get() + 1L;
        final long cursor = ringBuffer.getCursor();
        final ArrayList<E> snapshot = new ArrayList<>();

        for (long seq = start; seq <= cursor; seq++) {
            if (!ringBuffer.isAvailable(seq)) {
                break;
            }
            E v = ringBuffer.get(seq).value;
            if (v != null) snapshot.add(v);
        }
        return snapshot.iterator();
    }

    @Override
    public void clear() {
        while (poll() != null) {
            // drain
        }
    }

    /**
     * ThreadPoolExecutor 可能会调用 remove(task)（例如取消任务时）。
     * RingBuffer 上“随机删除”不适合做（会破坏顺序/连续性），因此这里返回 false，避免抛异常干扰执行器。
     */
    @Override
    public boolean remove(Object o) {
        return false;
    }

    private static int normalizeToPowerOfTwo(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        int n = 1;
        while (n < capacity) {
            n <<= 1;
            if (n <= 0) {
                throw new IllegalArgumentException("capacity too large: " + capacity);
            }
        }
        return n;
    }
}
