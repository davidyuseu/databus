import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sy.common.concurrent.queue.MpscArrayQueue;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.message.metadata.Metadata;
import sy.databus.process.entity.TestEmptyMessage;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.MessageSeriesProcessor;
import sy.databus.process.frame.handler.decoder.FixedLenFrameDecoder;

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
public class MpscArrayQueueTest {
    private static final short SEAT_NUM = 1;
    private static final int TASK_COUNT = 1 << 24;
    private static final int PRODUCER_COUNT = 4;
    private static final byte[] BYTES = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    // 起始处理器，异步处理器 disposeInQueue == true
    MessageSeriesProcessor processor = new MessageSeriesProcessor();
    // 尾处理器，同步处理器
    MessageSeriesProcessor tailProcessor = new MessageSeriesProcessor();

    ExecutorService serviceJDK;
    ExecutorService serviceLZDP;

    //    @Setup(Level.Iteration) // 每次迭代前后
    @Setup(Level.Trial)
    public void setUp() {
        //        processor.appendServiceHandler(new FixedLenFrameDecoder(4));
        processor.initialize();
        tailProcessor.initialize();
        tailProcessor.appendServiceHandler(new AbstractTransHandler<IMessage>() {

            @Override
            public void handle(IMessage msg) throws Exception {
//                System.out.println(SByteUtil.logByteBuf((ByteBuf) msg.getData()));
                msg.release();
            }

            @Override
            public void initialize() {
                this.name = "TailProcessor";
            }
        });
        processor.addNextProcessor(tailProcessor);
        processor.setAsynchronous(true);
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


    @BeforeEach
    void init(){
        setUp();
    }
    // 简单测试处理链的运作
    @Test
    void simpleTest() throws Exception {
        serviceLZDP = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768));
        processor.setExecutor(serviceLZDP);


        processor.appendServiceHandler(new FixedLenFrameDecoder(4));
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-1");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-1";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-2");
                fireNext(msg);
            }

            @Override
            public void initialize() {
                this.name = "handler-2";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-3");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-3";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-4");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-4";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-5");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-5";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-6");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-6";
            }
        });
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-7");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-7";
            }
        });

        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(16);
        buf.writeBytes(BYTES);
        Metadata metadata = new Metadata(0L);
        EfficientMessage message = new EfficientMessage(metadata, buf);

        processor.handle(message);
    }

    @Benchmark
    public void DisruptorConsume() throws InterruptedException {
        serviceDisruptor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscDisruptorQueue<Runnable>(32768));

        processor.setExecutor(serviceDisruptor);

        Thread p1 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Disruptor_Producer1");

        Thread p2 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Disruptor_Producer2");

        Thread p3 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Disruptor_Producer3");

        Thread p4 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Disruptor_Producer4");

        p1.start();
        p2.start();
        p3.start();
        p4.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();

        Future future = serviceDisruptor.submit(() -> {
            // empty task
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        serviceDisruptor.shutdownNow();
    }

    @Benchmark
    public void JDKConsume() throws InterruptedException {
        serviceJDK = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        processor.setExecutor(serviceJDK);
        Thread p1 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "LBQ_Producer1");

        Thread p2 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "LBQ_Producer2");

        Thread p3 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "LBQ_Producer3");

        Thread p4 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "LBQ_Producer4");

        p1.start();
        p2.start();
        p3.start();
        p4.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();

        Future future = serviceJDK.submit(()-> {
            // empty task
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        serviceJDK.shutdownNow();
    }

    @Benchmark
    public void LZDPNonBlockingConsume() throws InterruptedException {
        serviceLZDP = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768));

        processor.setExecutor(serviceLZDP);
        Thread p1 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Mpsc_Producer1");

        Thread p2 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Mpsc_Producer2");

        Thread p3 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Mpsc_Producer3");

        Thread p4 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                try {
                    produceMsg();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "Mpsc_Producer4");

        p1.start();
        p2.start();
        p3.start();
        p4.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();

        Future future = serviceLZDP.submit(()-> {
            // empty task
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        serviceLZDP.shutdownNow();
    }
}