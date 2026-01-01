package sy.databus.process.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import javafx.beans.property.SimpleLongProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.IEvent;
import sy.databus.entity.ReFileTask;
import sy.databus.entity.ReplayFileItem;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.signal.*;
import sy.databus.global.WorkMode;
import sy.databus.organize.TaskManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.Console;
import sy.databus.process.DelayPreferenceGroup;
import sy.databus.process.IEventProc;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.fsm.GuardedObject;
import sy.databus.process.fsm.producer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.ALWAYS_TRANSITIVE;
import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.analyse.ReadingMode.*;


@Log4j2
abstract public class FileReplayer extends AbstractMessageProcessor<ByteBuf> {

    @Getter @Setter
    private ReadingMode mode = ALLIN;

//    @Getter @Setter
//    private ByteBuf fileBuf;

    private static FileChannel fileChannel = null;

    @Getter @Setter
    private File crntFile;

    @Setter @Getter
    private int indexToRead = 0;

    protected DelayPreferenceGroup prefGroup = new DelayPreferenceGroup();

    @Getter @Setter
    @Console(config = STATIC, display = "设备优选")
    protected boolean toPreferDev = false;

    private boolean single = false; // 单个读取

    private volatile boolean batch = true; // 分析模式下批处理

    protected volatile boolean singleLoop = true; // 单文件循环

    @Getter @Setter
    private List<ReplayFileItem> fileItems; //= new ArrayList<>(32);

    protected Interceptor interceptor = null;

    protected volatile boolean workingLoop = true; // 回放器读帧循环标识

    /** 该回放器是利用LockSupport.parkNanos方法来实现一段段将数据回放出来的，
     * 所以在跳转时间、暂停、关闭等操作时为了立即响应，使用LockSupport.unpark方法来唤醒*/
    private GuardedObject<Thread> crntThdGuarded = new GuardedObject<>();

    /** 在TailHandler中使用，见{@link #initialize()}中setTailHandler方法 */
    protected IEventProc<IEvent> next; // in OutBoundedHandler

    @Getter
    protected long lastFileTime = 0L;

    @Getter
    private final ProducerCommonCondition condition = new ProducerCommonCondition(
            new RUNNABLE(
                    () -> { // toOpen
                        toOpen();
                        FileReplayer.this.condition
                                .setFuture(executor.submit(FileReplayer.this::extractByChannel)); // 设置下一状态的future
                        crntThdGuarded.get();
                    },
                    () -> { // toShutdown
                        // do nothing
                    }),
            new RUNNING(
                    () -> { // toPark
                        if (mode == REPLAY) {
                            LockSupport.unpark(crntThdGuarded.get());
                        }
                        if (interceptor != null && interceptor.getLatch().getCount() > 0) {
                            log.error("The last count of latch should be 0 when go to park!");
                        }
                        interceptor = new Interceptor();
                        addInternalHandler(0, interceptor);
                    },
                    () -> { // toClose
                        if (mode == REPLAY) {
                            LockSupport.unpark(crntThdGuarded.get());
                        }
                        toClose();
                    },
                    () -> { // toShutdown
                        if (mode == REPLAY) {
                            LockSupport.unpark(crntThdGuarded.get());
                        }
                        toClose();
                        setClosingFuture(FileReplayer.this.condition.getFuture());
                    }
            ),
            new SUSPENDED(
                    () -> { // toOpen
                        toOpen();
                        if (mode == REPLAY) {
                            fileBaseTime = lastFileTime;
                            replayerBaseTime = CachedClock.instance().currentTimeMillis();
                        }
                        removeInternalHandler(interceptor);
                        interceptor.countDown();
                    },
                    () -> { // toClose
                        toClose();
                        removeInternalHandler(interceptor);
                        interceptor.countDown();
                    },
                    () -> { // toShutdown
                        toClose();
                        removeInternalHandler(interceptor);
                        interceptor.countDown();
                        setClosingFuture(FileReplayer.this.condition.getFuture());
                    }
            ),
            new TERMINATED(
                    () -> { // toOpen
                        // 模式切换时由ExecutorManager统一分配，已分配过的不再分配
                        // ExecutorManager.allocateExecutor(ReplayFileReader.this);

                        toOpen();
                        FileReplayer.this.condition
                                .setFuture(executor.submit(FileReplayer.this::extractByChannel)); // 设置下一状态的future
                        crntThdGuarded.get();
                    }
            )
    );

    private void toClose() { // toClose
        workingLoop = false;
    }

    private void toOpen() {
        workingLoop = true;
    }

    public boolean switchMode() {
        if (condition.getConLock().tryLock()) {
            try {
                if (condition.isRunnable()
                        || condition.isTerminated()) { // RUNNABLE或TERMINATED状态下才可以切模式
                    mode = mode == REPLAY ? ALLIN : REPLAY;
                    return true;
                } else {
                    return false;
                }
            } finally {
                condition.getConLock().unlock();
            }
        }
        return false;
    }

    @SneakyThrows
    @Override
    public void initialize() {
        super.initialize();
        setTailHandler(new AbstractTransHandler<>() {
            @Override
            public void initialize() {
                name = "匹配设备号转发器";
            }

            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                next.handle(msg);
            }
        });

        appendSlot(START.class, signal -> {
            condition.open();
            return true;
        });

        appendSlot(PAUSE.class, signal -> {
            condition.park();
            return true;
        });

        pileUpSlot(CLOSE.class, signal -> {
            condition.close();
            return true;
        });
    }

    private void fireFileReadBeginSig(ReFileTask task) {
        try {
            fireSignalAsSource(new FILE_READ_BEGIN(mode, task, this, ALWAYS_TRANSITIVE));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void fireFileReadEndSig(ReFileTask task) {
        try {
            fireSignalAsSource(new FILE_READ_END(this, task));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    // 文件基准时间
    protected long fileBaseTime = 0L;
    // 回放器基准时间
    protected long replayerBaseTime = 0L;
    // 当前待处理的文件
    protected ReplayFileItem crntFileItem;

    public void extractByChannel() {
        crntThdGuarded.complete(Thread.currentThread());
        if (nextProcessors.size() == 0) {
            log.warn("{} has no next processor, so produce method do nothing!",
                    this.getClass().getSimpleName());
            condition.selfClosing();
            return;
        }
        FileListLoop: for (int i = indexToRead; i < fileItems.size(); i++) {
            crntFileItem = fileItems.get(i);
            File dataFile = crntFileItem.getFile();
            if (dataFile == null) {
                log.warn("{} failed to read a null replay file!", this.getClass().getSimpleName());
                break;
            }
            try {
                fileChannel = new RandomAccessFile(dataFile, "r").getChannel();
                if (fileChannel.size() <= 0) {
                    log.error("the replay file is empty！");
                    break;
                }
                fileChannel.position(0);
            } catch (FileNotFoundException fileNotFoundException) {
                log.warn(fileNotFoundException);
                break;
            } catch (IOException e) {
                log.error(e);
                break;
            }

            if (mode == REPLAY) {
                crntFileItem.setTaskTip("任务文件 " + crntFileItem.getNum() + " 正在加载！");
                crntFileItem.setTaskTitle("任务" + crntFileItem.getNum() + "...");
                log.info("加载文件：{}_{}", crntFileItem.getNum(), crntFileItem.getName());
                // 预处理，获取时间桩
                var timeStakes = taskTimeStakes.get(crntFileItem);
                if (timeStakes == null) {
                    // 获取时间桩
                    timeStakes = getTimeStakes(fileChannel);
                    if (timeStakes == null || timeStakes.isEmpty()) {
                        log.error("Time Stakes is null or empty!");
                        break;
                    }
                    taskTimeStakes.put(crntFileItem, timeStakes);
                }
                // key step! clean the mappedByteBuffer in the future.
                crntFileItem.setTaskTip("任务文件 " + crntFileItem.getNum() + " 正在回放！");
                log.info("正在回放数据：{}_{}", crntFileItem.getNum(), crntFileItem.getName());
                do {
                    ReFileTask analysisFileTask = new ReFileTask(this.processorId, crntFileItem, mode);
                    fireFileReadBeginSig(analysisFileTask);
                    // 文件基准时间
                    fileBaseTime = timeStakes.get(0).time();
                    crntStartTime.set(fileBaseTime);
                    crntEndTime.set(timeStakes.get(timeStakes.size() - 1).time());
                    // 回放器基准时间
                    replayerBaseTime = System.currentTimeMillis();
                    /** close事件作用在readBuffer中的循环里，关闭工作，退出该方法，发射任务结束事件*/
                    try {
                        fileChannel.position(0);
                        replayBuffer(fileChannel);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                    }
                    // 自然执行完毕
                    if (!workingLoop) { // close()
                        analysisFileTask.setDoClosedLoop(true);
                        fireFileReadEndSig(analysisFileTask);
                        if (fileChannel != null) {
                            try {
                                fileChannel.close();
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                        break FileListLoop;
                    } else {
                        // 不希望在单文件循环时闭环此次任务（清理内存）
                        analysisFileTask.setDoClosedLoop(false);
                        fireFileReadEndSig(analysisFileTask);
                    }
                } while(singleLoop);
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            } else {
                crntStartTime.set(0);
                crntEndTime.set(0);
                crntChangedTime.set(0);

                // key step! clean the mappedByteBuffer in the future.
                ReFileTask analysisFileTask = new ReFileTask(this.processorId, crntFileItem, mode);
                crntFileItem.setTaskTip("任务文件 " + crntFileItem.getNum() + " 正在解析！");
                crntFileItem.setTaskTitle("任务" + crntFileItem.getNum() + "...");
                log.info("开始处理任务文件：{}_{}", crntFileItem.getNum(), crntFileItem.getName());
                fireFileReadBeginSig(analysisFileTask);
                /** close事件作用在readBuffer中的循环里，关闭工作，退出该方法，发射任务结束事件*/
                readBuffer(fileChannel);
                log.info("任务文件完成提取：{}_{}", crntFileItem.getNum(), crntFileItem.getName());
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
                // 自然执行完毕
                fireFileReadEndSig(analysisFileTask);
                /*直接释放MappedByteBuffer会导致映射空间访问错误
                SByteUtil.clean(mappedByteBuffer);*/
                /** 逐个文件处理，防止内存和cpu爆炸*/
                try {
                    TaskManager.waitFor(analysisFileTask);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                if (!workingLoop) { // close()
                    break;
                }
                if (!batch) // 单文件模式
                    break;
            }
        }
        /** Make sure it is about to exit the current method when calling this method.*/
        condition.selfClosing();
    }

    abstract protected void readBuffer(FileChannel fileChannel);

    /**
     * 拦截器，用于暂停 toPark()
     * */
    protected class Interceptor extends AbstractTransHandler<IMessage<ByteBuf>> {

        @Getter
        volatile CountDownLatch latch = new CountDownLatch(1);

        Interceptor() {
            setParentProcessor(FileReplayer.this);
        }

        @Override
        public void initialize() {
            this.name = "ClosingHandler";
            this.id = (byte) 0xff;
        }

        public void resetLatch() {
            if (latch == null || latch.getCount() == 0)
                latch = new CountDownLatch(1);
        }

        public void countDown() {
            if(latch != null)
                latch.countDown();
        }

        @Override
        public void handle(IMessage<ByteBuf> msg) throws Exception {
            latch.await();
            if (!workingLoop) { // close()
                msg.release();
                return;
            }
            fireNext(msg);
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        condition.shutdown();
        crntThdGuarded.complete(null);
    }

    protected record TimeStake(long time, int offset){}

    protected Map<ReplayFileItem, List<TimeStake>> taskTimeStakes = new HashMap<>();
    @Getter @JsonIgnore // 界面时间轴上的当前时间
    public SimpleLongProperty crntChangedTime = new SimpleLongProperty(0L);
    @Getter @JsonIgnore // 界面时间轴上的起始时间
    public SimpleLongProperty crntStartTime = new SimpleLongProperty(0L);
    @Getter @JsonIgnore // 界面时间轴上的结束时间
    public SimpleLongProperty crntEndTime = new SimpleLongProperty(0L);

    // 时间桩，至少获取两个桩，头桩和尾桩
    abstract protected List<TimeStake> getTimeStakes(FileChannel fileChannel);

    abstract protected void replayBuffer(FileChannel fileChannel) throws IOException;

    protected long timeToSkipAt = 0L;

    protected final Object skippingLocker = new Object();

    // 外部设置跳转时间
    public void skipToTimestamp(long time) {
        synchronized (skippingLocker) {
            LockSupport.unpark(crntThdGuarded.get());
            timeToSkipAt = time;
        }
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new WatchPaneShifter(this);
    }
}
