import io.netty.util.internal.shaded.org.jctools.queues.MpscUnboundedArrayQueue;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;


/**
 * java.lang.IllegalMonitorStateException - 如果当前的线程不是此对象锁的所有者，
 * 却调用该对象的notify()，notify()，wait()方法时抛出该异常。
 * 即：notify()，notify()，wait()，signal()，signalAll()方法都需要写在synchronized同步块或lock()和unlock()中。
 * 如：
 * ... ...
 * //        lock.lock();
 *             System.out.println("get lock!");
 *             singleConsumerWaitSet.signalAll(); => java.lang.IllegalMonitorStateException
 * //        lock.unlock();
 * ... ...
 * */

class WaitAndNotifyTest {

    @Test
    void test0() throws InterruptedException {
        Object lock = new Object();
        new Thread(() -> {
            synchronized (lock) {
                System.out.println("wait!");
                try {
                    lock.wait();
                    System.out.println("be notified!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(500);

        System.out.println(" to notify all!");
        lock.notify(); //java.lang.IllegalMonitorStateException: current thread is not owner
        Thread.sleep(500);
        System.out.println("main thread end!");
    }

    @Test
    void test1() throws InterruptedException {
        // 消费锁
        ReentrantLock lock = new ReentrantLock();
        Condition singleConsumerWaitSet = lock.newCondition();
        // 模拟消费者线程
        new Thread(() -> {
            lock.lock();
            System.out.println("wait!");
            try {
                System.out.println("lock!");
                Thread.sleep(2000);
                singleConsumerWaitSet.await();
                System.out.println("be notified!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }).start();

        Thread.sleep(500);
        System.out.println(" to notify all!");
        lock.lock();
            System.out.println("get lock!");
            singleConsumerWaitSet.signalAll();
        lock.unlock();
        Thread.sleep(500);
        System.out.println("main thread end!");

    }

    @Test
    void test2() {
        MpscUnboundedArrayQueue<Integer> queue = new MpscUnboundedArrayQueue<>(200);
        queue.offer(1);
        queue.offer(2);
        System.out.println(queue.size());
        queue.poll();
        System.out.println(queue.size());
    }

    @Test
    void test3() throws InterruptedException {
        Thread t = new Thread(() -> {
            System.out.println("t1 start!");
            LockSupport.park();
            System.out.println("t1 unPark!");
        });
        t.start();
        Thread.sleep(1000);
        System.out.println(t.getState());
    }

}
