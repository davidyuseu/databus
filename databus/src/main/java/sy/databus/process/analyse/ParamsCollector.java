package sy.databus.process.analyse;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.tmresolve.ResultStruct;
import sy.databus.entity.ReFileTask;

import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.property.RadioSelectObList_Ins;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.entity.signal.DATA_TASK_END;
import sy.databus.entity.signal.ISignal;
import sy.databus.global.Constants;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.organize.ExecutorManager;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.process.*;
import sy.databus.view.watch.ParamsCollectorWatchPane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import static sy.databus.global.Constants.MAX_PARAMS_ROWS;
import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.PARAMS_RECORDER,
        pane = ParamsCollectorWatchPane.class,
        coupledParents = {CharNumParamsAnalyzerTXT.class, GeneralParamsAnalyzerTXT.class, ParamsLazyGroupByFlagTXT.class}
)
public class ParamsCollector extends AbstractIntegratedProcessor {

    private static final int BATCH_LINE_NUM = 256; // 每256行批处理一次（写入一次）

    // TODO 时区可配置
    protected final DateFormat dateFormat2Mn4File = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.CHINA); //"yyyyMMdd_HHmmss-SSS"
    protected final DateFormat dateFormat2Day4File = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);

    protected final DateFormat dateFormatToMm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.CHINA);

    // 当前处理到基准时间戳列表的索引
    int[] timeListIndex = new int[]{0};

    protected FileWriter fileWriter = null;

    @Setter(AccessLevel.NONE) @JsonIgnore
    protected int rowCount = 0;//当前data有多少行

    private String crntReFileName = "";

    // 同步门栓
    private CountDownLatch refLatch = null;

    @Getter
    private SSyncObservableList<ResultStruct> sumParams = new SSyncObservableList<>(this.procMutex);

    @Setter @Getter
    @Console(config = STATIC, display = "时间格式")
    private RadioSelectObList<String> timeFormat;

    protected TimeFormatter timeFormatter = null;

    @Setter @Getter
    @Console(config = STATIC, display = "输入设备")
    private RadioSelectObList_Ins inputProcessors;

    abstract class DataFusionTask implements Runnable {
        protected CountDownLatch latch;

        public void setLatch(CountDownLatch latch) {
            this.latch = latch;
        }
    }
    record DataFusionIssue(ParamsAnalyzer analyzer
            , ParamsAnalyzer.CachingFileReader fileReader
            , boolean timeRef
            , List<StringBuilder> paramBatch
            , DataFusionTask task){}

    /**
     * false: 监视 true: 记录
     * */
    @Setter @Getter
    private boolean toRecord = true;

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

        if (inputProcessors == null) {
            inputProcessors = new RadioSelectObList_Ins(false, -1);
        }
        inputProcessors.setCandidateList(this.previousProcessors);

        appendSlot(DATA_TASK_BEGIN.class, this::startTask);

        pileUpSlot(DATA_TASK_END.class, signal -> {
            if (!toRecord) // 仅监视
                return true;
            ParamsAnalyzer timeBasisProc = (ParamsAnalyzer) inputProcessors.getSelectedItem();
            // 基准时间戳列表
            List<Long> timeBasis = timeBasisProc.getTimeBasis();
            if (timeBasis == null) {
                log.warn("无基准时间线数据：{}", timeBasisProc.getNameValue());
                return false;
            }
            List<DataFusionIssue> dataFusionIssues = new ArrayList<>();
            // 创建批处理任务
            if (!createBatchTasks(dataFusionIssues, timeBasisProc, timeBasis))
                return false;
            // 创建线程池
            ExecutorService executor = createTasksService();
            // 开始执行任务
            execTasks(dataFusionIssues, timeBasis, executor);
            // 关闭写流
            finish();
            // 关闭线程池
            executor.shutdownNow();
            // 关闭各ParamAnalyzer中的读通道
            clearIssues(dataFusionIssues);
            return true;
        });
    }

    private void clearIssues(List<DataFusionIssue> dataFusionIssues) {
        for (var issue : dataFusionIssues) {
            try {
                if (issue.fileReader.readChan() != null) {
                    issue.fileReader.readChan().force(false);
                    issue.fileReader.readChan().close();
                }
                if (issue.fileReader.readRAF() != null) {
                    issue.fileReader.readRAF().close();
                }

            } catch (IOException /*| InterruptedException*/ e) {
                log.error(e.getMessage());
            }
        }
        // clean the paramBatch
        dataFusionIssues.clear();
    }

    private boolean startTask(ISignal signal) {
        if (!toRecord) // 仅监视
            return true;
        crntReFileName = "writer";
        if (signal instanceof DATA_TASK_BEGIN sig)
            crntReFileName = sig.getTaskName();
        return startTask0();
    }

    private boolean startTask0() {
        String dirPath = Constants.TABLE_TXT_DIR_PATH;
        dirPath += dateFormat2Day4File.format(CachedClock.instance().currentTimeMillis()) + "\\"
                + crntReFileName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dirPath + "\\"
                + getNameValue() + "_"
                + dateFormat2Mn4File.format(CachedClock.instance().currentTimeMillis()) + ".txt");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file, Constants.gCharset);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    private boolean createBatchTasks(List<DataFusionIssue> dataFusionIssues, ParamsAnalyzer timeBasisProc, List<Long> timeBasis) {
        DataFusionIssue timeBasisIssue;
        for (AbstractIntegratedProcessor previousProcessor : previousProcessors) {
            ParamsAnalyzer proc = (ParamsAnalyzer) previousProcessor;
            if (!proc.isHasParamSelected()) // 没有选择待解析参数的解参器不参与数据融合任务
                continue;
            var fileReader = proc.createFileReader();
            if (fileReader == null)
                continue;
            List<StringBuilder> paramBatch = new ArrayList<>(BATCH_LINE_NUM);
            // initialize paramBatch
            for (int k = 0; k < BATCH_LINE_NUM; k++) {
                paramBatch.add(new StringBuilder());
            }
            DataFusionTask task = null;
            if (proc instanceof ParamsAnalyzerTXT analyzerTXT) {
                // 返回实际批处理了多少行参数
                task = new DataFusionTask() {
                    @Override
                    public void run() {
                        int count = Math.min(BATCH_LINE_NUM, timeBasis.size() - timeListIndex[0]);
                        try {
                            for (int n = 0; n < count; n++) {
                                analyzerTXT.fetchRowInCache(timeBasis.get(timeListIndex[0] + n)
                                        , paramBatch.get(n), fileReader);
                            }
                        } catch (Exception e) {
                            log.error(e.getMessage());
                        }
                        latch.countDown();
                    }
                };
                if (proc == timeBasisProc) {// 当前为时间戳基准处理
                    timeBasisIssue = new DataFusionIssue(analyzerTXT, fileReader,true, paramBatch, task);
                    dataFusionIssues.add(0, timeBasisIssue); // 把基准任务放在首位置
                } else {
                    dataFusionIssues.add(new DataFusionIssue(analyzerTXT, fileReader,false, paramBatch, task));
                }
            } else {
                log.error("{} cannot support this type of ParamsAnalyzer as previous processor to collect params!",
                        this.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    private ExecutorService createTasksService() {
        int nThreads = Math.min(previousProcessors.size(), ExecutorManager.CPU_CORES_NUM);
        ExecutorService executor = Executors.newFixedThreadPool(nThreads);
        return executor;
    }

    private void execTasks(List<DataFusionIssue> dataFusionIssues, List<Long> timeBasis, ExecutorService executor) {
        try {
            // 用来批追加BATCH_LINE_NUM行参数文本
            StringBuilder batchBuilder = new StringBuilder();
            batchBuilder.append(ParamsAnalyzer.FIRST_HEAD);
            // 追加各表头
            for (var issue : dataFusionIssues) { // createBatchTasks方法中已将基准任务放在首位，故其余位置删除FIRST_HEAD列
                batchBuilder.append(
                        ((ParamsAnalyzerTXT)issue.analyzer).headerRow
                                .substring(ParamsAnalyzer.FIRST_HEAD.length()));
            }
            batchBuilder.append("\n");
            String head = batchBuilder.toString();
            fileWriter.write(batchBuilder.toString());
            for (timeListIndex[0] = 0; timeListIndex[0] < timeBasis.size(); timeListIndex[0] += BATCH_LINE_NUM) {
                refLatch = new CountDownLatch(dataFusionIssues.size());
                for (var issue : dataFusionIssues) {
                    issue.task.setLatch(refLatch);
                    executor.execute(issue.task);
                }
                refLatch.await();
                int count = Math.min(BATCH_LINE_NUM, timeBasis.size() - timeListIndex[0]);
                batchBuilder.setLength(0);
                for (int i = 0; i < count; i++) { // 一次性追加多行
                    rowCount++;
                    timeFormatter.format(batchBuilder, timeBasis.get(timeListIndex[0] + i)); // 追加时间戳
                    for (var task : dataFusionIssues) {
                        batchBuilder.append(task.paramBatch.get(i)); // 追加第n个子任务的第i行
                    }
                    batchBuilder.append("\n"); //每行追加一个换行符
                }
                //每追加完BATCH_LINE_NUM或剩余行时写入一次
                fileWriter.write(batchBuilder.toString());

                if (rowCount >= MAX_PARAMS_ROWS) {
                    finish();
                    startTask0();
                    fileWriter.write(head);
                }
            }
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        if (inputProcessors.getCandidateList().isEmpty())
            return;

        if (inputProcessors.getSelPId() != -1) {
            for (int i = 0; i < previousProcessors.size(); i++) {
                if (inputProcessors.getSelPId()
                        == previousProcessors.get(i).getProcessorId().codec()) {
                    inputProcessors.setSelIndex(i);
                }
            }
        }

        if (inputProcessors.getSelectedItem() == null)
            inputProcessors.setSelIndex(0);
        var selProc = inputProcessors.getSelectedItem();
        if (selProc == null) {
            ProcessorInitException e = new ProcessorInitException("A null processor in the input Processors!");
            log.error(e.getMessage());
            throw e;
        } else {
            if (selProc instanceof ParamsAnalyzer analyzer) {
                analyzer.setTimeReference(true);
            } else {
                log.error("There is a unsupported type of input processor!");
            }
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
    protected AbstractInfoReporter createInfoReporter() {
        return new WatchPaneShifter(this);
    }

    public void finish() {
        if (fileWriter != null) {
            try {
                fileWriter.flush();
                fileWriter.close();
                fileWriter = null;
                rowCount = 0;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        var processor = inputProcessors.getSelectedItem();
        if (processor instanceof ParamsAnalyzer analyzer) {
            analyzer.setTimeReference(false);
        } else {
            log.error("There is a unsupported type of input processor!");
        }
    }

    @Override
    public boolean validateAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        return previousProcessor instanceof ParamsAnalyzer;
    }

    @Override
    public boolean validateAsOutput(@NonNull AbstractIntegratedProcessor nextProcessor) {
        return false;
    }

    @Override
    public void connectedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        super.connectedAsInput(previousProcessor);
        if (previousProcessor instanceof ParamsCorrelating paramsCorrelating) {
            paramsCorrelating.setRemoteList(sumParams);
        } else throw new ProcessorInitException(ParamsCollector.class.getSimpleName() +
                "can only connect to " + ParamsCorrelating.class.getSimpleName());
    }

    @Override
    public void detachedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        super.detachedAsInput(previousProcessor);
        if (previousProcessor instanceof  ParamsCorrelating paramsCorrelating) {
            paramsCorrelating.unbindToRemoteList(sumParams);
        } else throw new ProcessorInitException(ParamsCollector.class.getSimpleName() +
                "can only connect to " + ParamsCorrelating.class.getSimpleName());
    }
}
