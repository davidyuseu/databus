package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.tmresolve.gen.GeneralParamParser;
import sy.databus.entity.property.MultiSelGenParamList;
import sy.databus.entity.property.SFile;
import sy.databus.global.ProcessorType;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import java.util.ArrayList;
import java.util.List;

import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.PARAMS_RECORDER,
        pane = MessageSeriesProcessorWatchPane.class
)
public class GeneralParamsAnalyzerCSV extends ParamsAnalyzerCSV {

    public GeneralParamsAnalyzerCSV(){}

    protected GeneralParamParser generalParamParser = new GeneralParamParser();

    @Setter @Getter
    @Console(config = STATIC, display = "通用解参表")
    private MultiSelGenParamList multiSelGenParamList = new MultiSelGenParamList();

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
                multiSelGenParamList.getCandidateList()
                        .setAll(generalParamParser.getParaTable());
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
    protected List<List<String>> createTableWithHeaders() {
        int paramCount = multiSelGenParamList.getSelectedList().size();
        List<List<String>> table = new ArrayList<>();
        // 创建表头
        List<String> headColumn1 = new ArrayList<>(paramCount + 32);
        headColumn1.add(FIRST_HEAD);
        table.add(headColumn1);

        for (int i = 0; i < paramCount; i++) {
            ArrayList<String> headColumn = new ArrayList<>();
            headColumn.add(multiSelGenParamList.getSelectedList().get(i).getItemName());
            table.add(headColumn);
        }

        addAdditionalParamToHead(table);

        return table;
    }

    protected void addAdditionalParamToHead(List<List<String>> head) {

    }

    @Override
    protected void writeRowToFile(long timestamp, ByteBuf frameBuf) throws Exception {
        int paramCount = multiSelGenParamList.getSelectedList().size();
        ArrayList<String> itemRow = new ArrayList<>(paramCount + 32);
        itemRow.add(timestamp == 0
                ? String.valueOf(rowCount)
                : dateFormatToMm.format(timestamp));
        // TODO 与使用updateParams比较区别
        generalParamParser.collectStrResults(startPos, frameBuf,
                multiSelGenParamList.getSelectedList(),
                itemRow,
                timestamp);

        addAdditionalData(generalParamParser, frameBuf, itemRow);
        csvData.add(itemRow);
        rowCount++;
    }

    @Override
    protected void cleanParamsResults() {
        multiSelGenParamList.getSelectedList().forEach(res -> res.setStrResult(""));
    }


    protected void addAdditionalData(GeneralParamParser generalParamParser, ByteBuf frameBuf, ArrayList<String> itemRow) {

    }

}
