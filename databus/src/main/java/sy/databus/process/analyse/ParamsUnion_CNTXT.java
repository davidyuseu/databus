package sy.databus.process.analyse;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.property.AbstractMultiSelectObList;
import sy.databus.entity.property.SFile;
import sy.databus.global.ProcessorType;
import sy.databus.global.WorkMode;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.WatchPaneShifter;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Processor;
import sy.databus.view.watch.ParamsUnionWatchPane;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Log4j2
@Processor(
        type = ProcessorType.FRAME,
        pane = ParamsUnionWatchPane.class
)
public class ParamsUnion_CNTXT extends AbstractIntegratedProcessor {

    private Set<AbstractMultiSelectObList> charUnionSelections = new HashSet<>();

    private Set<AbstractMultiSelectObList> numUnionSelections = new HashSet<>();

    private File charProtocolFile = null;

    private File numProtocolFile = null;

    @Override
    public void initialize() {
        super.initialize();
        asynchronous = false; // 工具组件不需要执行器
    }

    @Override
    public void boot() throws Exception {
        super.boot();
        previousProcessors.forEach(proc -> {
            if (proc instanceof CharNumParamsAnalyzerTXT cnParser) {
                if (cnParser.getMultiSelNumParamList() != null)
                    cnParser.getMultiSelCharParamList().setUnionSelections(charUnionSelections);
                if (cnParser.getMultiSelCharParamList() != null)
                    cnParser.getMultiSelNumParamList().setUnionSelections(numUnionSelections);
            }
        });
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new WatchPaneShifter(this);
    }

    @Override
    public boolean validateAsOutput(@NonNull AbstractIntegratedProcessor nextProcessor) {
        return false;
    }

    @Override
    public boolean validateAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        if (previousProcessor instanceof CharNumParamsAnalyzerTXT cnParser) {
            if (charProtocolFile == null && numProtocolFile == null) {
                if (cnParser.getCharProtocolFile().equals(SFile.DEFAULT_EMPTY_FILE)
                        && cnParser.getNumProtocolFile().equals(SFile.DEFAULT_EMPTY_FILE)) {
                    log.warn("{} 未加载任何参数表，无法联合选参！", cnParser.getNameValue());
                    return false;
                }
                try {
                    charProtocolFile = new File(cnParser.getCharProtocolFile().getCanonicalPath());
                    numProtocolFile = new File(cnParser.getNumProtocolFile().getCanonicalPath());
                    return true;
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return false;
                }
            } else {
                boolean pass = false;
                try {
                    pass = Objects.equals(charProtocolFile.getCanonicalPath(), cnParser.getCharProtocolFile().getCanonicalPath())
                            && Objects.equals(numProtocolFile.getCanonicalPath(), cnParser.getNumProtocolFile().getCanonicalPath());
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return false;
                }
                if (!pass) {
                    log.warn("待联合参数表须与已联合参数表相同！");
                }
                return pass;
            }
        } else {
            return false;
        }
    }

    @Override
    public void connectedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        super.connectedAsInput(previousProcessor);
        if (previousProcessor instanceof CharNumParamsAnalyzerTXT cnParser) {
            var selCharParamList = cnParser.getMultiSelCharParamList();
            reselectParams(selCharParamList, charUnionSelections);
            var selNumParamList = cnParser.getMultiSelNumParamList();
            reselectParams(selNumParamList, numUnionSelections);
        }
    }

    private void reselectParams(AbstractMultiSelectObList selParamList, Set<AbstractMultiSelectObList> unionSelections) {
        if (selParamList != null) {
            selParamList.setUnionSelections(unionSelections);
            unionSelections.add(selParamList);
            if (unionSelections.size() > 1) {
                for (var baseList : unionSelections) {
                    if (baseList.getSelectedList().size() > 0 && selParamList != baseList) {
                        selParamList.clearSelected();
                        for (var toSelIndex : baseList.getSelectedIndexes()) {
                            selParamList.select((int) toSelIndex);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void detachedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        super.detachedAsInput(previousProcessor);
        if (previousProcessor instanceof CharNumParamsAnalyzerTXT cnParser) {
            var selCharParamList = cnParser.getMultiSelCharParamList();
            disconnectWithUnion(selCharParamList, charUnionSelections);
            var selNumParamList = cnParser.getMultiSelNumParamList();
            disconnectWithUnion(selNumParamList, numUnionSelections);
            if (previousProcessors.size() == 0) {
                charProtocolFile = null;
                numProtocolFile = null;
            }
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {

    }

    private void disconnectWithUnion(AbstractMultiSelectObList selParamList
            , Set<AbstractMultiSelectObList> unionSelections) {
        if (selParamList != null) {
            unionSelections.remove(selParamList);
            selParamList.setUnionSelections(null);
        }
    }
}
