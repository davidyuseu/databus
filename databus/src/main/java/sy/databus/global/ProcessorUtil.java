package sy.databus.global;

import java.util.concurrent.locks.LockSupport;

public class ProcessorUtil {
    public static final int RETRIES_WAITFOR = 150,
            SPIN_THRESHOLD = 100;
    private static final long SLEEP_TIME_NS = 1000000L;
    public static int waitFor(int loopCnt) {
        if (loopCnt > SPIN_THRESHOLD) {
            --loopCnt;
            Thread.onSpinWait();
        } else if (loopCnt > 0) {
            --loopCnt;
            Thread.yield();
        } else {
            LockSupport.parkNanos(SLEEP_TIME_NS);
        }
        return loopCnt;
    }
}
