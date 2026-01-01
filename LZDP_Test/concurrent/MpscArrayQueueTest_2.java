import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sy.common.concurrent.queue.MpscArrayQueue;
import sy.databus.entity.message.IMessage;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.MessageSeriesProcessor;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class MpscArrayQueueTest_2 {
    private static final short SEAT_NUM = 1;
    private static final int TASK_COUNT = 1 << 24;
    private static final int PRODUCER_COUNT = 4;
    private static final byte[] BYTES = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    // 起始处理器，异步处理器 disposeInQueue == true
    MessageSeriesProcessor processor = new MessageSeriesProcessor();
    // 尾处理器，同步处理器
    MessageSeriesProcessor tailProcessor = new MessageSeriesProcessor();

//    ExecutorService serviceMpsc;
//    ExecutorService serviceMpsc_Park;
//    ExecutorService serviceMpsc_ParkTime;
//    ExecutorService serviceLBQ;

    @Setup(Level.Trial)
    public void setUp() {
        //        processor.appendServiceHandler(new FixedLenFrameDecoder(4));

        processor.addNextProcessor(tailProcessor);
        processor.setAsynchronous(true);


    }

    @BeforeEach
    void init(){
        setUp();
    }

    //    @Test
    void simpleTest() {
        MpscArrayQueuePark<Runnable> sMpscQueue = new MpscArrayQueuePark<Runnable>(32768);
        ExecutorService serviceMpsc = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                sMpscQueue);
        serviceMpsc.execute(() -> {
            sMpscQueue.setCustomer(Thread.currentThread());
        });
    }

    private void produceMsg() throws Exception {

        TestEmptyMessage emptyMessage = new TestEmptyMessage();
        processor.handle(emptyMessage);
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

    private void multiProduceTaskTest(ExecutorService executorService, String executorName) throws InterruptedException {
//        CountDownLatch countDownLatch = new CountDownLatch(1);
        processor.setExecutor(executorService);

        tailProcessor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            int count = 0;

            @Override
            public void handle(IMessage msg) throws Exception {
//                System.out.println(SByteUtil.logByteBuf((ByteBuf) msg.getData()));
                count ++;
//                System.out.println(count);
                msg.release();
                if (count == TASK_COUNT)
                    System.out.println(count);
//                    countDownLatch.countDown();
            }

            @Override
            public void initialize() {
                this.name = "TailProcessor";
            }
        });

        Thread p1 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT ; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, executorName + "_Producer1");

        Thread p2 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT ; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, executorName + "_Producer2");

        Thread p3 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT ; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, executorName + "_Producer3");

        Thread p4 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT ; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, executorName + "_Producer4");

        p1.start();
        p2.start();
        p3.start();
        p4.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();

        Future future = executorService.submit(()-> {
            // empty task
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        executorService.shutdownNow();
    }

    @Benchmark
    @Test
    public void serviceLBQTest() throws InterruptedException {
        // 初始化LBQ
        ExecutorService serviceLBQ = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        multiProduceTaskTest(serviceLBQ, "serviceLBQ");
    }

    @Benchmark
    @Test
    public void serviceMpscTest() throws InterruptedException {
        // 初始化MpscQueue
        ExecutorService serviceMpsc = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768));
        multiProduceTaskTest(serviceMpsc, "serviceMpsc");
    }
    @Benchmark
    @Test
   public void serviceMpsc_SyncNotifyTest() throws InterruptedException {
        // 初始化Mpsc_SyncNotify
       ExecutorService mpsc_SyncNotify = new ThreadPoolExecutor(1, 1,
               0L, TimeUnit.MILLISECONDS,
               new MpscArrayQueueSyncNotify<>(32768));
       multiProduceTaskTest(mpsc_SyncNotify, "mpsc_SyncNotify");
   }
    @Benchmark
    @Test
   public void serviceMpsc_TrylockTest() throws InterruptedException {
       // 初始化Mpsc_Trylock
       ExecutorService mpsc_Trylock = new ThreadPoolExecutor(1, 1,
               0L, TimeUnit.MILLISECONDS,
               new MpscArrayQueueTrylock<>(32768));
       multiProduceTaskTest(mpsc_Trylock, "mpsc_Trylock");
   }

    @Benchmark
    @Test
    public void serviceMpsc_ParkTest() throws InterruptedException {
        ExecutorService serviceMpsc_Park;
        // 初始化MpscQueue_Park
        MpscArrayQueuePark<Runnable> sMpscQueue = new MpscArrayQueuePark<Runnable>(32768);
        serviceMpsc_Park = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                sMpscQueue);
        Future future = serviceMpsc_Park.submit(()-> {
            sMpscQueue.setCustomer(Thread.currentThread());
        });
        multiProduceTaskTest(serviceMpsc_Park, "serviceMpsc_Park");
    }


    @Benchmark
    @Test
    public void serviceMpsc_ParkTimeTest() throws InterruptedException {
        // 初始化MpscQueue_ParkTime;
        ExecutorService serviceMpsc_ParkTime = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueueParkTime<>(32768));
        multiProduceTaskTest(serviceMpsc_ParkTime, "serviceMpsc_ParkTime");
    }
}
