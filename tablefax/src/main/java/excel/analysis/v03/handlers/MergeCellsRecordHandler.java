package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.enums.CellExtraTypeEnum;
import excel.metadata.CellExtra;
import org.apache.poi.hssf.record.MergeCellsRecord;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class MergeCellsRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public boolean support(XlsReadContext xlsReadContext, Record record) {
        return xlsReadContext.readWorkbookHolder().getExtraReadSet().contains(CellExtraTypeEnum.MERGE);
    }

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        MergeCellsRecord mcr = (MergeCellsRecord)record;
        for (int i = 0; i < mcr.getNumAreas(); i++) {
            CellRangeAddress cellRangeAddress = mcr.getAreaAt(i);
            CellExtra cellExtra = new CellExtra(CellExtraTypeEnum.MERGE, null, cellRangeAddress.getFirstRow(),
                cellRangeAddress.getLastRow(), cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
            xlsReadContext.xlsReadSheetHolder().setCellExtra(cellExtra);
            xlsReadContext.analysisEventProcessor().extra(xlsReadContext);
        }
    }
}
