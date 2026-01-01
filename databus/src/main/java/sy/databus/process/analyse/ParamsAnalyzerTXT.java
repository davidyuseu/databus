package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.IMessage;

import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.global.Constants;
import sy.databus.global.WorkMode;

import sy.databus.process.Console;
import sy.databus.process.TimeFormatter;
import sy.databus.process.frame.AbstractTransHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static sy.databus.global.Constants.MAX_PARAMS_ROWS;
import static sy.databus.process.Console.Config.STATIC;


@Log4j2
abstract public class ParamsAnalyzerTXT extends ParamsAnalyzer
        implements ParamsCorrelating {

    protected FileWriter fileWriter = null;

    protected StringBuilder lastRowBuilder = new StringBuilder();
    protected File crntFileToWrite = null;

    @Setter @Getter
    @Console(config = STATIC, display = "时间格式")
    private RadioSelectObList<String> timeFormat;

    protected TimeFormatter timeFormatter = null;

    @Override
    public void initialize() {
        super.initialize();

        if (timeFormat == null) {
            timeFormat = new RadioSelectObList<>(false, 0);
            timeFormatter = (builder, timestamp) -> {
                builder.append(dateFormatToMm.format(timestamp)).append("\t");
                return builder;
            };
        }
        timeFormat.getCandidateList().addAll("标准时间戳"
                , "行号"
                , "整型(帧计数)");

        appendSlot(DATA_TASK_BEGIN.class, signal -> {
            if (dataFusion && toRecord)
            createHead();
            return true;
        });
    }

    protected void tailHandle(EfficientMessage message) {
        if (fixLen > 0 && fixLen != message.dataCapacity()) {
            log.error("'fixedLen' of {} is different from the actual message len!",
                    this.getClass().getSimpleName());
            message.release();
            return;
        }
        try {
            if (dataFusion) { // 数据融合模式
                if (toRecord) { // 记录
                    if (fixLen > 0) {
                        cacheFixedLenMsgAndRelease(message);
                    } else {
                        log.warn("'fixLen' must be set!");
                        message.release();
                    }
                } else { // 仅解参，监视参数
                    parseRowIntoResult(message.getMetadata().getBirthTimestamp(), message.getData());
                    message.release();
                }
            } else {
                long timestamp = message.getMetadata().getBirthTimestamp();
                writeRowToFile(timestamp, message.getData());
                message.release();
                if (rowCount >= MAX_PARAMS_ROWS) {
                    finish();
                    prepare(crntFileName);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            handleExceptionMsg(message);
        }
    }

    public void finish() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
                fileWriter = null;
                rowCount = 0;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    abstract public void loadConfigFile() throws Exception;

    public void prepare(String rFileName) throws Exception {
        if(rFileName == null || rFileName.isEmpty()) {
            rFileName = "writer";
        }

        String dirPath = Constants.TABLE_TXT_DIR_PATH
                + dateFormat2Day4File.format(CachedClock.instance().currentTimeMillis()) + "\\"
                + rFileName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        crntFileToWrite = new File(dirPath + "\\"
                + name.get() + "_"
                + dateFormat2Mn4File.format(CachedClock.instance().currentTimeMillis()) + ".txt");

        if (crntFileToWrite.exists())
            crntFileToWrite.delete();

        try {
            crntFileToWrite.createNewFile();
            fileWriter = new FileWriter(crntFileToWrite, Constants.gCharset);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // create head and construct the headerRow
        createHead();
        // headerRow appends "\n" and write it to file
        fileWriter.write(headerRow.append("\n").toString());
        fileWriter.flush();
    }
    // 空行（不带换行符）的构成应该在接收到Begin信号后，根据所选参数数目追加制表符，并在收到End信号后重新清空
    @Getter
    protected StringBuilder emptyRow = new StringBuilder();
    // 表头行（不带换行符）的构成应该在接收到Begin信号后，根据所选参数名称追加形成，并在收到End信号后重新清空
    @Getter
    protected StringBuilder headerRow = new StringBuilder();

    // construct the 'emptyRow' and 'headerRow'
    abstract protected void createHead();

    abstract protected void setRow(long timestamp, ByteBuf frameBuf, StringBuilder rowBuilder) throws Exception;

    abstract protected void setRowWithTimestamp(long timestamp, ByteBuf frameBuf, StringBuilder rowBuilder) throws Exception;

    /** call the {@link #fetchFixedLenDataFromCache} to parse a params line*/
    public void fetchRowInCache(long timestamp, StringBuilder fetchedBuilder, CachingFileReader fileReader) throws Exception {
        fetchedBuilder.setLength(0);
        ByteBuf frame = fetchFixedLenDataFromCache(timestamp, fileReader.readChan(), fileReader.readRAF());
        if (frame == null) {
            fetchedBuilder.append(lastRowBuilder); // 保留上一帧结果
        } else {
            setRow(timestamp, frame, fetchedBuilder);
            lastRowBuilder = fetchedBuilder;
            frame.release();
        }
    }

    @Override
    public void writeRowToFile(long timestamp, ByteBuf frameBuf) throws Exception {
        if (fileWriter == null)
            return;

        setRowWithTimestamp(timestamp, frameBuf, lastRowBuilder);
        fileWriter.write(lastRowBuilder.append("\n").toString());
        rowCount++;
        fileWriter.flush();
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        try {
            loadConfigFile();
        } catch (Exception e) {
            passable = false;
            log.error(e);
        }
        int timeFormatSel = timeFormat.getSelIndex();
        switch (timeFormatSel) {
            case 1 -> timeFormatter = (builder, timestamp)
                    -> builder.append(rowCount).append("\t");
            case 2 -> timeFormatter = (builder, timestamp)
                    -> builder.append(timestamp).append("\t");
            default -> timeFormatter = (builder, timestamp)
                    -> builder.append(dateFormatToMm.format(timestamp)).append("\t");
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        super.reset(oriMode, targetMode);
        lastRowBuilder.setLength(0);
    }

}
