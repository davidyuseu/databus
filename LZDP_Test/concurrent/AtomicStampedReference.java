AtomicStampedReference
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicStampedReference;

public class AtomicStampedReference {
    private static AtomicInteger atomicInteger = new AtomicInteger(100);

    private static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<Integer>
            (100,0);

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(()->{
            atomicInteger.compareAndSet(100,101);
            atomicInteger.compareAndSet(101,100);
        });
        Thread thread2 = new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean atomicResult = atomicInteger.compareAndSet(100,101);
            System.out.println("ABA£¿AtomicInteger£º" + atomicResult);
        });
        thread1.start();;
        thread2.start();;
        thread1.join();
        thread2.join();

        Thread stamped1 = new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            atomicStampedReference.compareAndSet(100,101,
                    atomicStampedReference.getStamp(),atomicStampedReference.getStamp() + 1);
            atomicStampedReference.compareAndSet(101,100,
                    atomicStampedReference.getStamp(),atomicStampedReference.getStamp() + 1);
            System.out.println("Thread stamped1 ver= " + atomicStampedReference.getStamp());
        });

        Thread stamped2 = new Thread(()->{
            int stamp = atomicStampedReference.getStamp();
            System.out.println("Before ABA, Thread stamped2 previous Ver= " + atomicStampedReference.getStamp());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean atomicSyampedResult = atomicStampedReference.compareAndSet(100,101,
                    stamp,stamp + 1);
            System.out.println("When ABA occurs£¬AtomicStampedReference£º" + atomicSyampedResult);
        });
        stamped1.start();
        stamped2.start();
    }
}