package sy.databus.process.fsm;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GuardedObject <T> {
    // result
    private T response;

    // acquire the result
    public T get() {
        synchronized (this) {
            // non res
            while(response == null) {
                try {
                    this.wait();
                    Thread.yield();
                } catch (InterruptedException e) {
                    log.warn(e);
                }
            }
            return response;
        }
    }

    // set the result
    public void complete(T t) {
        synchronized (this) {
            this.response = t;
            this.notifyAll();
        }
    }
}
