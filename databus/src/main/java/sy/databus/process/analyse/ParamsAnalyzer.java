package sy.databus.process.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.ReFileTask;
import sy.databus.entity.ReplayFileItem;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.signal.*;
import sy.databus.global.WorkMode;
import sy.databus.process.Console;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.MessageSeriesProcessor;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import static sy.databus.process.Console.Config.STATIC;

/**
 * */
@Log4j2
abstract public class ParamsAnalyzer extends MessageSeriesProcessor<ByteBuf> {

    private static int TO_WRITE_SIZE = 1024 * 1024 * 16; // 16MB

    public static String FIRST_HEAD = "No.\t";

    private static int TIMESTAMP_LONG_SIZE = 8;
    @Setter @Getter
    @Console(config = STATIC, display = "表头项前缀")
    protected String headPrefix = "";

    @Setter(AccessLevel.NONE) @JsonIgnore
    protected int rowCount = 0;//当前data有多少行

    protected final DateFormat dateFormatToMm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.CHINA);
    protected final DateFormat dateFormatToDay = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);

    // TODO 时区可配置
    protected final DateFormat dateFormat2Mn4File = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.CHINA); //"yyyyMMdd_HHmmss-SSS"
    protected final DateFormat dateFormat2Day4File = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);

    // 本次任务是否已经设置了时区
    protected boolean timeZoneSet = false;

    protected String crntFileName = "";

    protected boolean dataFusion = false;

    // determined by the ParamsCollector
    @Getter @Setter @JsonIgnore
    private boolean timeReference = false;

    @Getter @JsonIgnore
    private List<Long> timeBasis = null;

    // 数据融合的总帧数
    private int numOfFrames = 0;
    // 已缓存的字节数（每10M刷盘一次）
    private long bytesSize = 0;
    // RAF的当前读指针
    protected int reader = 0;
    // 定长解析帧（若等于0，则按变长协议缓存帧）
    @Setter @Getter
    @Console(config = STATIC, display = "缓存解帧长度(0为变长)")
    protected int fixLen = 0;

    @Getter @JsonIgnore
    protected File cachingFile = null;

    protected RandomAccessFile writeRAF = null;
    protected FileChannel writeChannel = null;

    @Getter @JsonIgnore
    protected boolean hasParamSelected = false;

    abstract protected boolean checkIfHasParamSelected();

    protected boolean toRecord = false;

    protected String startTime = null;

    protected String endTime = null;

    protected boolean crntTaskContinuity = false;

    // call by ParamsCollector, 如果有缓存文件，且大小>0则返回true，否则返回false
    record CachingFileReader(File cachingFile, AtomicInteger cachingCount, RandomAccessFile readRAF, FileChannel readChan) {}
    private AtomicInteger cachingFileCount;
    public CachingFileReader createFileReader() {
        if (cachingFile == null)
            return null;
        try {
            RandomAccessFile readRAF = new RandomAccessFile(cachingFile, "r");
            if (readRAF.length() <= 0) {
                readRAF.close();
                return null;
            }
            FileChannel readChannel = readRAF.getChannel();
            cachingFileCount.incrementAndGet();
            return new CachingFileReader(cachingFile, cachingFileCount, readRAF, readChannel);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    private void refreshCachingOp() {
        numOfFrames = 0;
        reader = 0;

        try {
            cachingFile = new File(".\\dataFusionCaches\\cachedData_"
                    + ParamsAnalyzer.this.getProcessorId().getProcessorCode() + "_"
                    + CachedClock.instance().currentTimeMillis()
                    + ".dat");

            while (!cachingFile.createNewFile()) {
                log.warn("retry to create cachingFile!");
            }
            cachingFileCount = new AtomicInteger(0);
            cachingFile.deleteOnExit();
            writeRAF = new RandomAccessFile(cachingFile, "rw");
            writeChannel = writeRAF.getChannel();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    protected boolean isContiguousOrNew(ReplayFileItem fileItem) {
        String sTime = fileItem.getStartTime();
        String eTime = fileItem.getEndTime();
        if (sTime != null && !sTime.isEmpty() &&
                eTime != null && !eTime.isEmpty()) {
            if (startTime == null && endTime == null || sTime.equals(endTime)) {
                startTime = sTime;
                endTime = eTime;
                return true;
            }
        }
        startTime = null;
        endTime = null;
        return false;
    }

    private void finishCaching() {
        try {
            if (writeChannel != null) {
                writeChannel.force(false);
                writeChannel.close();
                writeChannel = null;
            }
            if (writeRAF != null) {
                writeRAF.close();
                writeRAF = null;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    protected void cacheFixedLenMsgAndRelease(EfficientMessage message) throws IOException {
        if (writeRAF == null) {
            message.release();
            return;
        }
        ByteBuf buf = message.getData();
        long timestamp = message.getMetadata().getBirthTimestamp();
        if (timeReference)
            timeBasis.add(timestamp);
        numOfFrames++;
        // 若没有选择待解析参数，则不写入数据融合缓存文件
        if (!hasParamSelected) {
            message.release();
            return;
        }
        writeRAF.writeLong(timestamp);
        writeChannel.write(buf.nioBuffer());

        bytesSize = writeChannel.size();
        if (bytesSize >= TO_WRITE_SIZE) {
            writeChannel.force(false);
        }
        message.release();
    }

    @Override
    public void initialize() {
        super.initialize();

        asynchronous = true;

        appendSlot(DATA_TASK_BEGIN.class, signal -> {
            boolean paramsPresent = checkIfHasParamSelected();
            if (!paramsPresent)
                log.warn("解参器'{}'没有选择待解参数！", this.getNameValue());
            if (dataFusion) {
                toRecord = nextProcessors.stream()
                        .anyMatch(processor -> processor instanceof ParamsCollector collector
                                && collector.isToRecord());
                if (toRecord) {
                    finishCaching();
                    refreshCachingOp();
                    if (timeReference) {// 作为时间基准
                        if (timeBasis == null) {
                            timeBasis = new ArrayList<>(1024);
                        } else timeBasis.clear();
                    }
                }
            } else {
                if (checkIfHasParamSelected())
                    finish();
                timeZoneSet = false;
                if (signal instanceof FILE_READ_BEGIN sig) {
                    crntFileName = sig.getTaskName();
                    var fileItem = ((ReFileTask) sig.getTask()).getReplayFileItem();
                    crntTaskContinuity = isContiguousOrNew(fileItem);
                    if (!crntTaskContinuity)
                        cleanParamsResults();
                } else if (signal instanceof DATA_TASK_BEGIN sig) {
                    crntFileName = sig.getTaskName();
                } else {
                    crntFileName = this.getNameValue();
                }
                try {
                    if (paramsPresent)
                        prepare(crntFileName);
                } catch (Exception e) {
                    log.error("Failed to creat the table file!");
                }
            }
            return true;
        });

        pileUpSlot(DATA_TASK_END.class, signal -> {
            if (dataFusion) {
                if (toRecord) {
                    finishCaching();
                }
            } else {
                // 若正在写的时候切换回放文件，也需要先发送文件结束信号
                if (checkIfHasParamSelected())
                    finish();
            }
            return true;
        });

        setTailHandler(new AbstractTransHandler<>() {
            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                if (msg instanceof EfficientMessage message) {
                    outBoundedStatistic(message);
                    tailHandle(message);
                } else {
                    log.error("Unsupported msg type!");
                    msg.release();
                }
            }

            @Override
            public void initialize() {
                name = "参数解析与记录";
            }
        });
    }

    protected abstract void tailHandle(EfficientMessage message);

    @Override
    public void boot() throws Exception {
        super.boot();
        dataFusion = nextProcessors.stream()
                .anyMatch(processor -> processor instanceof ParamsCollector);
    }

    // 由当前文件指针往后索引出定长data，并转为ByteBuf返回
    // 返回null的情况有两种：1. 缓存读完了，没有比基准时间戳大的数据了；2. 所有缓存数据的时间都比基准时间戳大
    /**
     * @param timestamp 基准时间列表中遍历出的一个时间值 （可理解为基准时间指针）
     * crntTime 缓存文件中读出的一个时间值
     * */
    protected ByteBuf fetchFixedLenDataFromCache(long timestamp, FileChannel readChannel, RandomAccessFile readRAF) throws IOException {
        try {
            while (readChannel.position() < readChannel.size()) {
                long crntTime = readRAF.readLong();
                if (timestamp > crntTime) { // 还需往后读，缓存指针向后移动
                    readChannel.position(readChannel.position() + fixLen);
                } else {
                    if (timestamp < crntTime) { /** 当前时间大于基准时间戳时，取上一次时间位置的帧*/
                        if (readChannel.position() >= TIMESTAMP_LONG_SIZE + fixLen) { // 缓存指针发生过移动
                            readChannel.position(
                                    readChannel.position() - TIMESTAMP_LONG_SIZE - fixLen);// 返回上一次时间位置的定长data
                        } else { // 缓存指针没有发生过移动，即缓存的起始时间戳目前大于基准时间指针
                            readChannel.position(0);
                            return null;
                        }
                    }
                    ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer(fixLen);
                    buf.writerIndex(fixLen);
                    readChannel.read(buf.nioBuffer());
                    return buf;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    abstract public void loadConfigFile() throws Exception;

    abstract public void prepare(String rFileName) throws Exception ;

    abstract protected void parseRowIntoResult(long timestamp, ByteBuf frameBuf) throws Exception;

    abstract protected void writeRowToFile(long timestamp, ByteBuf frameBuf) throws Exception ;

    abstract public void finish();

    abstract protected void cleanParamsResults();

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        super.reset(oriMode, targetMode);
        if (timeReference) { // 作为时间基准
            if (timeBasis != null) {
                timeBasis.clear();
                timeBasis = null;
            }
            timeReference = false;
        }
    }
}
