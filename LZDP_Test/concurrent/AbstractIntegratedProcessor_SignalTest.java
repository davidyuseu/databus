import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sy.common.concurrent.queue.MpscArrayQueue;
import sy.databus.entity.signal.START;
import sy.databus.entity.signal.Signal;
import sy.databus.process.frame.MessageSeriesProcessor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.ALWAYS_TRANSITIVE;
import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.RECV_ONLY_ONCE;

class AbstractIntegratedProcessor_SignalTest {
    static class SignalTestProcessor extends MessageSeriesProcessor {
        @Override
        public void initialize() {
            super.initialize();

            appendSlot(START.class, signal -> {
                System.out.println("response the signal of 'START'!");
                return true;
            });
        }
    }

    SignalTestProcessor signalTestProcessor = new SignalTestProcessor();
    MessageSeriesProcessor undertaker1 = new MessageSeriesProcessor();
    MessageSeriesProcessor undertaker2 = new MessageSeriesProcessor();

    @BeforeEach
    void setUp() {
        signalTestProcessor.initialize();
        undertaker1.initialize();
        undertaker2.initialize();
        undertaker1.setExecutor(new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768)));
        undertaker1.setAsynchronous(true);

        undertaker2.setAsynchronous(true);
        undertaker2.setExecutor(new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768)));

        signalTestProcessor.addNextProcessor(undertaker1);
        undertaker1.addNextProcessor(undertaker2);
    }

    /**
     * Test Report:
     * process =>
     *  handle the signal directly!
     *  response the signal of 'START'!
     *  emit the signal to next!
     *  async handle the signal!
     *  emit the signal to next!
     *  async handle the signal!
     *  no next signal handler!
     * */
    @Test
    void signal_START_ALWAYS_TRANSITIVE_test() throws Exception {
        signalTestProcessor.handle(new START(signalTestProcessor, ALWAYS_TRANSITIVE));
        signalTestProcessor.handle(new START(signalTestProcessor, ALWAYS_TRANSITIVE));
    }

    @Test
    void signal_START_Reconsumable_test() throws Exception {
        signalTestProcessor.handle(Signal.Builder.newSignalInstance(signalTestProcessor, START.class, ALWAYS_TRANSITIVE));
        signalTestProcessor.handle(Signal.Builder.newSignalInstance(signalTestProcessor, START.class, ALWAYS_TRANSITIVE));
    }

    /**
     * Test Report:
     * process =>
     *  handle the signal directly!
     *  response the signal of 'START'!
     *  end the signal
     *
     *  async handle the signal!
     *  end the signal
     * */
    @Test
    void signal_START_ONLY_ONCE_test() throws Exception {
        signalTestProcessor.handle(new START(signalTestProcessor, RECV_ONLY_ONCE));
        undertaker1.handle(new START(undertaker1, RECV_ONLY_ONCE));
    }
}