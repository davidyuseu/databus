import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sy.common.concurrent.queue.MpscArrayQueue;
import sy.databus.entity.message.IMessage;
import sy.databus.process.entity.TestEmptyMessage;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.MessageSeriesProcessor;

import java.util.concurrent.*;

class VolatileNextHandlerTest {
    private static final int TASK_COUNT = 1 << 24;
    private static final short SEAT_NUM = 1;
    private static final byte[] BYTES = new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    private static final int PRODUCER_COUNT = 4;

    // 起始处理器，异步处理器 disposeInQueue == true
    MessageSeriesProcessor processor = new MessageSeriesProcessor();
    // 尾处理器，同步处理器
    MessageSeriesProcessor tailProcessor = new MessageSeriesProcessor();

    @BeforeEach
    void init(){
        tailProcessor.appendServiceHandler(new AbstractTransHandler<IMessage>() {

            @Override
            public void handle(IMessage msg) throws Exception {
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
/*        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(16);
        buf.writeBytes(BYTES);
        Metadata metadata = new Metadata(0L);
        EfficientMessage message = new EfficientMessage(metadata, buf);
        processor.undertake(message);*/

        TestEmptyMessage emptyMessage = new TestEmptyMessage();
        processor.handle(emptyMessage);
    }

    @Test
    void serviceMpscTest() throws InterruptedException {
        ExecutorService serviceMpsc = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new MpscArrayQueue<Runnable>(32768));

        processor.setExecutor(serviceMpsc);

        AbstractTransHandler first = new AbstractTransHandler<IMessage<ByteBuf>>() {
            @Override
            public void handle(IMessage msg) throws Exception {
                System.out.println("handler-1");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-1";
            }
        };

        processor.appendServiceHandler(first);
        processor.appendServiceHandler(new AbstractTransHandler<IMessage>() {
            @Override
            public void handle(IMessage msg) throws Exception {
//                System.out.println("handler-2");
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
//                System.out.println("handler-3");
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
//                System.out.println("handler-4");
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
//                System.out.println("handler-5");
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
//                System.out.println("handler-6");
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
//                System.out.println("handler-7");
                fireNext(msg);
            }
            @Override
            public void initialize() {
                this.name = "handler-7";
            }
        });


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

        Thread p5 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                if (processor.getServiceHandlers().get(0).equals(first)) {
                    processor.getServiceHandlers().remove(first);
                }
            }
        });

        Thread p6 = new Thread(() -> {
            for (int i = 0; i < TASK_COUNT / PRODUCER_COUNT - 1; i++) {
                if (!processor.getServiceHandlers().get(0).equals(first)) {
                    processor.getServiceHandlers().add(0, first);
                }
            }
        });

        p1.start();
        p2.start();
        p3.start();
        p4.start();
        p5.start();
        p6.start();

        p1.join();
        p2.join();
        p3.join();
        p4.join();
        p5.join();
        p6.join();

        Future future = serviceMpsc.submit(()-> {
            // empty task
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        serviceMpsc.shutdownNow();
    }
}