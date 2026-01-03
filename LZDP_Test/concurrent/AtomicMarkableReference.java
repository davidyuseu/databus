AtomicMarkableReference
import java.util.concurrent.atomic.AtomicMarkableReference;

public class AtomicMarkableReferenceDemo {
    /**
     * initialization
     */
    private static final Integer INIT_NUM = 10;    
    private static final Integer TEMP_NUM = 20;
    private static final Integer UPDATE_NUM = 100;

    private static final Boolean INITIAL_MARK = Boolean.FALSE;

    private static AtomicMarkableReference atomicMarkableReference = new AtomicMarkableReference(INIT_NUM,INITIAL_MARK);

    public static void main(String[] args) {
        new Thread(()->{
            System.out.println(Thread.currentThread().getName() + " £ºInitial£º" + INIT_NUM + " £¬Marked as£º" + INITIAL_MARK);
            boolean mark = atomicMarkableReference.isMarked();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean result = atomicMarkableReference.compareAndSet(INIT_NUM,UPDATE_NUM,mark,Boolean.TRUE);
            System.out.println("After ABA, atomicMarkableReferences' Results = " + result);
        },"Thread A").start();

        new Thread(()->{
            Thread.yield();
            System.out.println(Thread.currentThread().getName() + " £ºInitial£º" + INIT_NUM + " £¬Marked as£º" + INITIAL_MARK);
            atomicMarkableReference.compareAndSet(atomicMarkableReference.getReference(),TEMP_NUM,
                    atomicMarkableReference.isMarked(),Boolean.TRUE);
            System.out.println(Thread.currentThread().getName() + " £º1st modified to£º"
                    + atomicMarkableReference.getReference() + " £¬±ê¼ÇÎª£º" + INITIAL_MARK);
            atomicMarkableReference.compareAndSet(atomicMarkableReference.getReference(),INIT_NUM,
                    atomicMarkableReference.isMarked(),Boolean.TRUE);
            System.out.println(Thread.currentThread().getName() + " £º2nd modified to£º"
                    + atomicMarkableReference.getReference() + " £¬Marked as£º" + INITIAL_MARK);
        },"Thread B").start();
    }

}