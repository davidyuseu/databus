package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.tmresolve.ResultStruct;
import sy.common.tmresolve.gen.GeneralParamParser;
import sy.databus.entity.property.MultiSelGenParamList;
import sy.databus.entity.property.SFile;
import sy.databus.global.ProcessorType;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import static sy.databus.process.Console.Config.DYNAMIC;
import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.PARAMS_RECORDER,
        pane = MessageSeriesProcessorWatchPane.class
)
public class GeneralParamsAnalyzerTXT extends ParamsAnalyzerTXT {

    public GeneralParamsAnalyzerTXT(){}

    protected GeneralParamParser generalParamParser = new GeneralParamParser();

    @Setter
    @Getter
    @Console(config = DYNAMIC, display = "通用解参表")
    private MultiSelGenParamList multiSelGenParamList;

    @Override
    public void setRemoteList(SSyncObservableList remoteList) {
        var selList = multiSelGenParamList.getSelectedList();
        if (selList != null && !selList.isEmpty()) {
            remoteList.addAll(selList);
        }
        multiSelGenParamList.setRemoteList(remoteList);
    }

    @Override
    public void unbindToRemoteList(SSyncObservableList remoteList) {
        remoteList.removeAll(multiSelGenParamList.getSelectedList());
        multiSelGenParamList.setRemoteList(null);
    }

    @Getter @Setter
    @Console(config = STATIC, display = "解参配置文件")
    private SFile protocolFile = SFile.buildDefaultFile("*.ini");

    @Getter @Setter
    @Console(config = STATIC, display = "起始字节位")
    private int startPos = 0;

    // 在接收到START信号时，检查是否有待解析参数
    @Override
    protected boolean checkIfHasParamSelected() {
        hasParamSelected = multiSelGenParamList.getSelectedList().size() > 0;
        return hasParamSelected;
    }

    @SneakyThrows
    @Override
    public void initialize() {
        super.initialize();

        if (multiSelGenParamList == null) {
            multiSelGenParamList = new MultiSelGenParamList();
        }

        multiSelGenParamList.setMutex(this.procMutex);
        generalParamParser.setQueryMutex(this.procMutex);

        protocolFile.setFileChangedActionAndDoOnce(file -> {
            loadConfigFile();
            multiSelGenParamList.limitedByFrameLen();
        });
    }

    @Override
    public void loadConfigFile() throws Exception {
        if (protocolFile.isFile() && protocolFile.exists()) {
            if (!protocolFile.equals(generalParamParser.getProtocolFile())) {
                try {
                    generalParamParser.readParaTable(protocolFile);
                } catch (Exception e) {
                    generalParamParser.setProtocolFile(null);
                    throw e;
                } finally {
                    multiSelGenParamList.clearItems();
                }
                multiSelGenParamList.setAllCandidateItems(generalParamParser.getParaTable());
            }
        }
    }

    @Override
    protected void parseRowIntoResult(long timestamp, ByteBuf frameBuf) throws Exception {
        generalParamParser.updateParams(startPos, frameBuf,
                multiSelGenParamList.getSelectedList(),
                timestamp);
    }

    @Override
    protected void cleanParamsResults() {
        multiSelGenParamList.getSelectedList().forEach(res -> res.setStrResult(""));
    }

    @Override
    protected void createHead() {
        headerRow.setLength(0);
        emptyRow.setLength(0);
        headerRow.append(FIRST_HEAD);
        for (ResultStruct res : multiSelGenParamList.getSelectedList()) {
            headerRow.append(headPrefix.trim()).append(res.getItemName()).append("\t");
            emptyRow.append("\t");
        }
        lastRowBuilder = emptyRow;
//        headerRow.append("\n");
    }

    @Override
    public void setRow(long timestamp, ByteBuf frameBuf, StringBuilder rowBuilder) throws Exception {
        rowBuilder.setLength(0);
        setRow0(timestamp, frameBuf, rowBuilder);
    }
    protected void setRow0(long timestamp, ByteBuf frameBuf, StringBuilder rowBuilder) throws Exception {
        generalParamParser.collectStrResults(startPos, frameBuf,
                multiSelGenParamList.getSelectedList(),
                rowBuilder,
                timestamp);
//        rowBuilder.append("\n");
    }

    @Override
    protected void setRowWithTimestamp(long timestamp, ByteBuf frameBuf, StringBuilder rowBuilder) throws Exception {
        rowBuilder.setLength(0);
        timeFormatter.format(rowBuilder, timestamp);
        setRow0(timestamp, frameBuf, rowBuilder);
    }


}
