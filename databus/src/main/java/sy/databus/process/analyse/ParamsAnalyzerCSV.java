package sy.databus.process.analyse;

import excel.ExcelWriter;
import excel.support.ExcelTypeEnum;
import excel.write.metadata.WriteSheet;
import excel.write.metadata.WriteWorkbook;
import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.message.EfficientMessage;
import sy.databus.global.WorkMode;
import sy.databus.process.Console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import static sy.databus.global.Constants.EXCEL_DIR_PATH;
import static sy.databus.global.Constants.MAX_PARAMS_ROWS;
import static sy.databus.process.Console.Config.STATIC;

@Log4j2
public abstract class ParamsAnalyzerCSV extends ParamsAnalyzer
        implements ParamsCorrelating {
    @Getter @Setter
    private String tableName;
    protected WriteSheet csvSheet = new WriteSheet();
    private WriteWorkbook workbook = new WriteWorkbook();

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private OutputStream outToCSV;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    protected ExcelWriter csvWriter;

    @Getter @Setter
    @Console(config = STATIC, display = "时区校准")
    private boolean needTimeZone = false;

    private static Map<String, String> fileTimeZones = new HashMap<>();

    protected List<ArrayList<String>> csvData = new ArrayList<>();

    private static final Object timeZoneSettingLock = new Object();

    private static String[] zoneIDs;
    static {
        zoneIDs = TimeZone.getAvailableIDs();
    }

    @Override
    public void initialize() {
        super.initialize();

        workbook.setExcelType(ExcelTypeEnum.XLSX);
    }

    protected void tailHandle(EfficientMessage message) {
        if (fixLen > 0 && fixLen != message.dataCapacity()) {
            log.error("'fixedLen' of {} is different from the actual message len!",
                    this.getClass().getSimpleName());
            message.release();
            return;
        }
        try{
            if (dataFusion) {
                if (toRecord) { // 记录
                    if (fixLen > 0)
                        cacheFixedLenMsgAndRelease(message);
                    else message.release();
                } else { // 仅解参，监视参数
                    long timestamp = message.getMetadata().getBirthTimestamp();
                    parseRowIntoResult(timestamp, message.getData());
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

    abstract public void loadConfigFile() throws Exception;

    public void prepare(String toDAFileName) {
        synchronized (this.procMutex) {
            if (toDAFileName == null || toDAFileName.isEmpty())
                toDAFileName = "writer";

            String csvDir = EXCEL_DIR_PATH
                    + dateFormat2Day4File.format(CachedClock.instance().currentTimeMillis()) + "\\"
                    + toDAFileName;
            File rFile = new File(csvDir);
            if (!rFile.exists()) {
                rFile.mkdirs();
            }

            //创建文件
            String csvName = csvDir + "\\"
                    + "ParamTable_"
                    + this.name.get() + "_"
                    + dateFormat2Mn4File.format(CachedClock.instance().currentTimeMillis())
                    + ".csv";

            try {
                outToCSV = new FileOutputStream(csvName);
            } catch (FileNotFoundException e) {
                log.error(e.getMessage());
            }
            workbook.setOutputStream(outToCSV);

            //        if(csvWriter == null)//每次必须创建新的ExcelWriter，否则finish()会报异常
            csvWriter = new ExcelWriter(workbook);
            csvSheet.setSheetName("sheet1");

            List<List<String>> table = createTableWithHeaders();

            csvSheet.setHead(table);
        }
    }

    abstract protected List<List<String>> createTableWithHeaders();

    abstract protected void writeRowToFile(long timestamp, ByteBuf frameBuf) throws Exception;

    public void finish(){
        synchronized (this.procMutex) {
            if (csvWriter != null && outToCSV != null) {
                csvWriter.write(csvData, csvSheet);
                csvWriter.finish();

                csvData.clear();
                csvWriter = null;
                rowCount = 0;
                outToCSV = null;
            } else {
                log.warn("'csvWriter' has or had been null, maybe it hasn't initialized or it has finished writing!");
            }
        }
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        try {
            loadConfigFile();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        super.reset(oriMode, targetMode);
    }

}
