package sy.databus.process.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue;
import javafx.beans.property.SimpleLongProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.DatabaseReadingTask;
import sy.databus.entity.IEvent;
import sy.databus.entity.RelatedInfo;
import sy.databus.entity.ReplayTaskItem;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.message.metadata.Metadata;
import sy.databus.entity.signal.DATABASE_READ_BEGIN;
import sy.databus.entity.signal.DATABASE_READ_END;
import sy.databus.global.ProcessorType;
import sy.databus.global.ProcessorUtil;
import sy.databus.global.WorkMode;
import sy.databus.organize.TaskManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.process.*;
import sy.databus.process.dev.UDPGroupRecvRcd;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.fsm.GuardedObject;
import sy.databus.process.fsm.producer.*;
import sy.databus.view.watch.DatabaseReplayerWatchPane;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import static sy.databus.process.AbstractIntegratedProcessor.RoutingPattern.ALWAYS_TRANSITIVE;
import static sy.databus.process.Console.Config.NON_RUNNING;
import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.analyse.ReadingMode.ALLIN;
import static sy.databus.process.analyse.ReadingMode.REPLAY;

@Log4j2
@Processor(
        type = ProcessorType.ORDINARY_FILE_READER,
        pane = DatabaseReplayerWatchPane.class
)
public class DatabaseReplayer extends AbstractMessageProcessor<ByteBuf> {

    @Getter @Setter
    private ReadingMode mode = ALLIN;

    private static String url = "jdbc:dm://localhost:";

    @Getter @Setter
    @Console(config = NON_RUNNING, display = "链路号筛选", reg = "^[0-9a-fA-F]{4}|$")
    private ByteBuf channelType;
    @Getter @Setter
    @Console(config = NON_RUNNING, display = "机号筛选", reg = "^[0-9a-fA-F]{8}|$")
    private ByteBuf craftNum;
    @Getter @Setter
    @Console(config = NON_RUNNING, display = "消息号筛选", reg = "^[0-9a-fA-F]{4}|$")
    private ByteBuf msgType;

    @Setter @Getter
    @Console(config = STATIC, display = "本地端口")
    private String localPort = "5236";
    @Setter @Getter
    @Console(config = STATIC, display = "key")
    private String pwd = "key";
    @Setter @Getter
    @Console(config = STATIC, display = "用户名")
    private String userid = "SYSDBA";

    protected DelayPreferenceGroup prefGroup = new DelayPreferenceGroup();

    @Getter @Setter
    @Console(config = STATIC, display = "设备优选")
    protected boolean toPreferDev = false;

    private GuardedObject<Thread> crntThdGuarded = new GuardedObject<>();

    protected IEventProc<IEvent> next;

    @Setter
    // 是否单任务回放
    protected volatile boolean singleTask = true;

    /*init when its nodeskin call the 'associateWith' */
    @Getter @Setter
    private List<ReplayTaskItem> taskItems;

    protected Interceptor interceptor = null;

    private final Map<Long, UDPGroupRecvRcd> nextProcessorMap = new HashMap<>();

    protected class Interceptor extends AbstractTransHandler<IMessage<ByteBuf>> {

        @Getter
        volatile CountDownLatch latch = new CountDownLatch(1);

        Interceptor() {
            setParentProcessor(DatabaseReplayer.this);
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
            if (!fetchLoop) { // close()
                msg.release();
                return;
            }
            fireNext(msg);
        }
    }


    @Getter
    private final ProducerCommonCondition condition = new ProducerCommonCondition(
            new RUNNABLE(
                    () -> { // toOpen
                        toOpen();
                        DatabaseReplayer.this.condition
                                .setFuture(executor.submit(DatabaseReplayer.this::extractData)); // 设置下一状态的future
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
                        interceptor = new DatabaseReplayer.Interceptor();
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
                        setClosingFuture(DatabaseReplayer.this.condition.getFuture());
                    }
            ),
            new SUSPENDED(
                    () -> { // toOpen
                        toOpen();
                        if (mode == REPLAY) {
                            notifiedTime = lt0 = DatabaseReplayer.this.t0;
                            lt1 = CachedClock.instance().currentTimeMillis();
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
                        setClosingFuture(DatabaseReplayer.this.condition.getFuture());
                    }
            ),
            new TERMINATED(
                    () -> { // toOpen
                        toOpen();
                        DatabaseReplayer.this.condition
                                .setFuture(executor.submit(DatabaseReplayer.this::extractData)); // 设置下一状态的future
                        crntThdGuarded.get();
                    }
            )
    );

    private void toClose() { // toClose
        fetchLoop = false;
    }

    private void toOpen() {
        fetchLoop = true;
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

    @Setter @Getter
    private int indexToRead = 0;

    @Getter
    protected ReplayTaskItem crntTaskItem;

    protected RelatedInfo relatedInfo = new RelatedInfo();

    @Getter @JsonIgnore // 时间轴上的当前时间
    public SimpleLongProperty crntChangedTime = new SimpleLongProperty(0L);
    @Getter @JsonIgnore // 时间轴上的起始时间
    public SimpleLongProperty crntStartTime = new SimpleLongProperty(0L);
    @Getter @JsonIgnore // 时间轴上的结束时间
    public SimpleLongProperty crntEndTime = new SimpleLongProperty(0L);

    @Setter
    private volatile int jumpPos = 1;
    @Setter
    private volatile long jumpTo = 0L;

    public void jumpTo(long time) {
        LockSupport.unpark(crntThdGuarded.get());
        jumpTo = time;
    }

    private record MsgRecord(int id,
                             long procId,
                             Timestamp recvAt,
                             byte[] channelType,
                             byte[] craftNum,
                             byte[] msgType,
                             byte[] frame) {
        @Override
        public String toString() {
            return "MsgRecord{" +
                    "id=" + id +
                    ", procId=" + procId +
                    ", recvAt=" + recvAt +
                    ", channelType=" + Arrays.toString(channelType) +
                    ", craftNum=" + Arrays.toString(craftNum) +
                    ", msgType=" + Arrays.toString(msgType) +
                    ", frame=" + Arrays.toString(frame) +
                    '}';
        }
    }

    @Getter
    private volatile long t0 = 0L, lt0 = 0L, lt1 = 0L, notifiedTime = 0L;
    // 缓冲队列
    private volatile SpscLinkedQueue<MsgRecord> bufferQueue;
    private volatile boolean fetchLoop = false;

    private static String sqlBatchFetch_f1 = "SELECT * FROM ",
            sqlBatchFetch_f2 = " WHERE id > ?",
            sqlFilterChanType = " AND channel_type = ?",
            sqlFilterCraftNum = " AND craft_num = ?",
            sqlFilterMsgType = " AND msg_type = ?",
            sqlBatchFetch_f3 = " ORDER BY id ASC LIMIT ",

            sqlGetJumpPos_f1 = "SELECT id, recvAt FROM ",
            sqlGetJumpPos_f2 = " WHERE recvAt > ?",
            sqlGetJumpPos_f3 = " ORDER BY recvAt ASC LIMIT 1"; // LIMIT ?

    private static String sqlGetFirstAndLast_f1 = "SELECT id, recvAt FROM ",
            sqlGetFirstAndLast_f2 = " WHERE 1=1",
            sqlGetFirst_f = " ORDER BY id ASC LIMIT 1",
            sqlGetLast_f = " ORDER BY id DESC LIMIT 1";

    private volatile boolean fetcherAlive = false;
    private static final int BATCH_SIZE = 4096,
            HALF_BATCH_SIZE = BATCH_SIZE / 2;
    private volatile boolean suspendedTrans = false;
    private final Object jumpLocker = new Object();
    // TODO 考虑与主执行器的信号传递
    private class BatchTransmitter extends Thread {

        private final DatabaseReadingTask readingTask;

        private BatchTransmitter(DatabaseReadingTask task) {
            this.readingTask = task;
        }

        @Override
        public void run() {
            try {
                replayData();
            } catch (InterruptedException e) {
                log.error("Failed to replay the task data! because: {}", e.getMessage());
                fetchLoop = false;
            }
            // 结束信号
            fireEndSig(readingTask);
        }

        private void suspendTrans() throws InterruptedException {
            synchronized (jumpLocker) {
                suspendedTrans  = true;
                jumpLocker.notifyAll();
                jumpLocker.wait();
            }
        }

        private void replayData() throws InterruptedException {
            int loopCnt = ProcessorUtil.RETRIES_WAITFOR;
            MsgRecord record;
            while (fetchLoop) {
                while (jumpTo > 0) {
                    suspendTrans();
                }
                if ((record = bufferQueue.poll()) == null) {
                    if (crntMsgId == finalId) // 标识退出，同时可防止尾部猝死
                        break;
                    loopCnt = ProcessorUtil.waitFor(loopCnt);
                    continue;
                }
                loopCnt = ProcessorUtil.RETRIES_WAITFOR;
                crntMsgId = record.id;
                t0 = record.recvAt.getTime();
                toDelay();
                notifiedTime = notifiedTiming(notifiedTime);
                next = nextProcessorMap.get(record.procId);
                if (next != null) {
                    ByteBuf buf = Unpooled.wrappedBuffer(record.frame);
                    EfficientMessage msg = new EfficientMessage(new Metadata(t0), buf);
                    try {
                        fireNext(msg);
                        next = null;
                    } catch (Exception e) {
                        log.error(e.getMessage());
                        handleExceptionMsg(msg);
                        return;
                    }
                }
            }
        }
    }

    private int pageOffset = 1, finalId = 0;;
    private int setTime(PreparedStatement pstmt,
                        SimpleLongProperty time) throws Exception {
        var ress = pstmt.executeQuery();
        if (ress.next()) {
            time.set(ress.getTimestamp("recvAt").getTime());
            return ress.getInt("id");
        }
        throw new Exception("Failed to find the first/final id, please check the filter conditions!");
    }

    private boolean fetchData(PreparedStatement ps) throws Exception {
        ps.setInt(1, pageOffset);
        var rss = ps.executeQuery();
        MsgRecord msg = null;
        while (rss.next()) {
            msg = new MsgRecord(
                    rss.getInt(1),
                    rss.getLong(2),
                    rss.getTimestamp(3),
                    rss.getBytes(4),
                    rss.getBytes(5),
                    rss.getBytes(6),
                    rss.getBytes(7)
            );
            bufferQueue.offer(msg);
        }
        if (msg != null) {
            pageOffset = msg.id;
            return msg.id != finalId;
        }
        return false;
    }
    public void extractData() {
        crntThdGuarded.complete(Thread.currentThread());
        if (nextProcessors.size() == 0) {
            log.warn("{} has no next processor, so produce method do nothing!",
                    this.getClass().getSimpleName());
            condition.selfClosing();
            return;
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url + localPort, userid, pwd);
            int i = indexToRead;
            TaskListLoop:
            while (i < taskItems.size()) {
                crntTaskItem = taskItems.get(i);
                var taskTable = crntTaskItem.getTaskTable();
                String sqlFetch = sqlBatchFetch_f1 + taskTable + sqlBatchFetch_f2;
                String sqlJump = sqlGetJumpPos_f1 + taskTable + sqlGetJumpPos_f2;
                String sqlGetFirst, sqlGetLast;
                sqlGetFirst = sqlGetLast = sqlGetFirstAndLast_f1 + taskTable + sqlGetFirstAndLast_f2;
                if (channelType != null) {
                    sqlFetch += sqlFilterChanType;
                    sqlJump += sqlFilterChanType;
                    sqlGetFirst += sqlFilterChanType;
                    sqlGetLast += sqlFilterChanType;
                }
                if (craftNum != null) {
                    sqlFetch += sqlFilterCraftNum;
                    sqlJump += sqlFilterCraftNum;
                    sqlGetFirst += sqlFilterCraftNum;
                    sqlGetLast += sqlFilterCraftNum;
                }
                if (msgType != null) {
                    sqlFetch += sqlFilterMsgType;
                    sqlJump += sqlFilterMsgType;
                    sqlGetFirst += sqlFilterMsgType;
                    sqlGetLast += sqlFilterMsgType;
                }
                sqlFetch += sqlBatchFetch_f3 + BATCH_SIZE;
                sqlJump += sqlGetJumpPos_f3;
                sqlGetFirst += sqlGetFirst_f;
                sqlGetLast += sqlGetLast_f;

                var psFetch = conn.prepareStatement(sqlFetch);
                var psJump = conn.prepareStatement(sqlJump);
                var psGetFirst = conn.prepareStatement(sqlGetFirst);
                var psGetLast = conn.prepareStatement(sqlGetLast);
                int idx = 1;
                if (channelType != null) {
                    psGetFirst.setBytes(idx, channelType.array());
                    psGetLast.setBytes(idx, channelType.array());
                    psFetch.setBytes(++idx, channelType.array());
                    psJump.setBytes(idx, channelType.array());
                }
                if (craftNum != null) {
                    psGetFirst.setBytes(idx, craftNum.array());
                    psGetLast.setBytes(idx, craftNum.array());
                    psFetch.setBytes(++idx, craftNum.array());
                    psJump.setBytes(idx, craftNum.array());
                }
                if (msgType != null) {
                    psGetFirst.setBytes(idx, msgType.array());
                    psGetLast.setBytes(idx, msgType.array());
                    psFetch.setBytes(++idx, msgType.array());
                    psJump.setBytes(idx, msgType.array());
                }

                finalId = setTime(psGetLast, crntEndTime);
                pageOffset = setTime(psGetFirst, crntStartTime);

                relatedInfo.setTaskTitle("任务 " + i + "...");
                log.info("加载数据表：{}_{}", i, taskTable);
                // 开始信号
                var readingTask = new DatabaseReadingTask(this.processorId, crntTaskItem, mode,
                        () -> {
                            relatedInfo.setTaskTip("任务数据表 '" + crntTaskItem.getTaskTable() + "' 已完成处理！");
                            log.info("任务数据表 '{}' 已完成处理!", crntTaskItem.getTaskTable());
                        });
                if (!fireBeginningSig(readingTask))
                    break;
                // 自旋拉取数据
                fetchLoop = true;
                jumpPos = 1;
                int loopCnt = ProcessorUtil.RETRIES_WAITFOR;
                /** 必须新建消费队列和消费者 */
                bufferQueue = new SpscLinkedQueue<>();
                var transmitter = new BatchTransmitter(readingTask);
                if (fetchData(psFetch)) {
                    // 初始化现场时间线
                    notifiedTime = t0 = lt0 = crntStartTime.get();
                    // 对齐当前时间线
                    lt1 = CachedClock.instance().currentTimeMillis();
                    // 开启数传线程
                    transmitter.start();
                    relatedInfo.setTaskTip("任务数据 " + taskTable + " 正在回放！");
                    while (fetchLoop) {
                        if (jumpTo > 0) {// 数传线程挂起
                            bufferQueue.clear();
                            suspendFetching(transmitter);
                            // 取最近id
                            psJump.setTimestamp(1, new Timestamp(jumpTo));
                            try {
                                var rss1 = psJump.executeQuery();
                                rss1.next();
                                jumpPos = rss1.getInt("id");
                                notifiedTime = lt0 = rss1.getTimestamp("recvAt").getTime();
                                lt1 = CachedClock.instance().currentTimeMillis();
                                crntMsgId = pageOffset = jumpPos;
                                jumpTo = 0;
                                var flag = fetchData(psFetch);
                                resumeTrans();
                                if (!flag) break;
                            } catch (Exception e) {
                                log.error("Failed to find the jumping id!because: {}", e.getMessage());
                                break;
                            }
                        } else if (pageOffset - crntMsgId < HALF_BATCH_SIZE) {
                            if (!fetchData(psFetch))
                                break;
                            loopCnt = ProcessorUtil.RETRIES_WAITFOR;
                        } else {
                            loopCnt = ProcessorUtil.waitFor(loopCnt);
                        }
                    }
                }
                syncCloseTrans(transmitter);
                if (!singleTask) // 是否单任务循环
                    i++;
                if (!fetchLoop)
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            resumeTrans();
            condition.selfClosing();
            try {
                conn.close();
            } catch (SQLException sqlException) {
                log.error("An error occurred when the connection was closed.because: {}",
                        sqlException.getMessage());
            }
            bufferQueue = null; // 不驻留，交GC
        }
    }

    private void syncCloseTrans(BatchTransmitter transmitter) {
        try {
            transmitter.join();
            TaskManager.waitFor(transmitter.readingTask); /* 同步闭环，防止资源爆炸*/
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    private void resumeTrans() {
        synchronized (jumpLocker) {
            suspendedTrans = false;
            jumpLocker.notifyAll();
        }
    }

    private void suspendFetching(Thread transmitter) throws InterruptedException {
        LockSupport.unpark(transmitter);
        synchronized (jumpLocker) {
            while (!suspendedTrans) {
                jumpLocker.wait();
            }
        }
    }

    private void timeChange(long lastTime) {
        crntChangedTime.set(lastTime);
    }
    private static final long UNIT_PARK_NANOS = 1000000L,
            NOTIFIED_PERIOD = 500L;

    private volatile int crntMsgId = 0;

    private void toDelay() {
        long delay = 0;
        do {
            var delta0 = t0 - lt0;
            var delta1 = CachedClock.instance().currentTimeMillis() - lt1;
            delay = delta0 - delta1;
            if (delay > 0)
                LockSupport.parkNanos(UNIT_PARK_NANOS * delay);
        } while (delay > 0);
    }

    private long notifiedTiming(long notifiedTime) {
        if (t0 - notifiedTime >= NOTIFIED_PERIOD) {
            timeChange(t0);
            notifiedTime = t0;
        }
        return notifiedTime;
    }

    private boolean fireBeginningSig(DatabaseReadingTask task) {
        try {
            fireSignalAsSource(new DATABASE_READ_BEGIN(task, this, ALWAYS_TRANSITIVE));
        } catch (Exception e) {
            log.error("Failed to begin with '{}'，because: {}",
                    task.getReplayTaskItem().getTaskTable(), e.getMessage());
            return false;
        }
        return true;
    }

    private void fireEndSig(DatabaseReadingTask task) {
        try {
            fireSignalAsSource(new DATABASE_READ_END(this, task));
        } catch (Exception e) {
            log.error("Failed to end with '{}'，because: {}",
                    task.getReplayTaskItem().getTaskTable(), e.getMessage());
        }
    }

    private static String sqlGetTasks =
            "SELECT * FROM t_tasks WHERE createdAt >= ? AND createdAt <= ?";
    public void refreshTasks(LocalDate startDate, LocalDate endDate) {
        taskItems.clear();
        try (var conn = DriverManager.getConnection(url, userid, pwd);
             var psmt = conn.prepareStatement(sqlGetTasks)) {
            psmt.setDate(1, Date.valueOf(startDate));
            psmt.setDate(2, Date.valueOf(endDate));
            try (var ress = psmt.executeQuery()) {
                while (ress.next()) {
                    ReplayTaskItem taskItem = new ReplayTaskItem(
                            ress.getInt("num"),
                            UUID.fromString(ress.getString("task_uid")),
                            ress.getString("processing_id"),
                            ress.getString("project_name"),
                            ress.getString("task_table"),
                            ress.getTimestamp("createdAt")
                    );
                    taskItems.add(taskItem);
                }
            }
        } catch (SQLException sqlException) {
            log.error("Failed to get task list! becaise: {}", sqlException.getMessage());
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        super.reset(oriMode, targetMode);
        taskItems.clear();
    }

    @Override
    public void initialize() {
        super.initialize();
        setTailHandler(new AbstractTransHandler<>() {
            @Override
            public void initialize() {
                name = "匹配设备PID转发器";
            }

            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                next.handle(msg);
            }
        });
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        for (var processor : nextProcessors) {
            if (processor instanceof UDPGroupRecvRcd udpGroupRecvRcd) {
                nextProcessorMap.put(udpGroupRecvRcd.getProcessorId().getProcessorCode(),
                        udpGroupRecvRcd);
            }
        }
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new WatchPaneShifter(this);
    }
}
