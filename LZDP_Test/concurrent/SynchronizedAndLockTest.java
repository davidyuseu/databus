import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Fork(1)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class SynchronizedAndLockTest {
    private static Object lock = new Object();
    private static ReentrantLock reentrantLock = new ReentrantLock();

    private static long cnt = 0;

    @Benchmark
    @Measurement(iterations = 2)
    @Threads(10)
    @Fork(0)
    @Warmup(iterations = 5, time = 10)
    public void testWithoutLock(){
        doSomething();
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Threads(10)
    @Fork(0)
    @Warmup(iterations = 5, time = 10)
    public void testReentrantLock(){
        reentrantLock.lock();
        doSomething();
        reentrantLock.unlock();
    }

    @Benchmark
    @Measurement(iterations = 2)
    @Threads(10)
    @Fork(0)
    @Warmup(iterations = 5, time = 10)
    public void testSynchronized(){
        synchronized (lock) {
            doSomething();
        }
    }

    private void doSomething() {
        cnt += 1;
        if (cnt >= (Long.MAX_VALUE >> 1)) {
            cnt = 0;
        }
    }

    public static void main(String[] args) throws RunnerException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("_yyyy-MM-dd HH-mm-ss");
        String fileName = dateTimeFormatter.format(LocalDateTime.now()) + "_benchmark.log";
        String className = new Object() {
            public String getClassName() {
                String clazzName = this.getClass().getName();
                return clazzName.substring(clazzName.lastIndexOf('.') + 1, clazzName.lastIndexOf('$'));
            }
        }.getClassName();
        String outputPath = "D:/Benchmark/" + className + "/";

        File dir = new File(outputPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Options options = new OptionsBuilder().include(className)
                .output(outputPath + fileName)
                .forks(1).build();
        new Runner(options).run();
    }

}
