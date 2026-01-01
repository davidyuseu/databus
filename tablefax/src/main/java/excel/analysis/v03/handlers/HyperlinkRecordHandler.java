package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.enums.CellExtraTypeEnum;
import excel.metadata.CellExtra;
import org.apache.poi.hssf.record.HyperlinkRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class HyperlinkRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {
    @Override
    public boolean support(XlsReadContext xlsReadContext, Record record) {
        return xlsReadContext.readWorkbookHolder().getExtraReadSet().contains(CellExtraTypeEnum.HYPERLINK);
    }

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        HyperlinkRecord hr = (HyperlinkRecord)record;
        CellExtra cellExtra = new CellExtra(CellExtraTypeEnum.HYPERLINK, hr.getAddress(), hr.getFirstRow(),
            hr.getLastRow(), hr.getFirstColumn(), hr.getLastColumn());
        xlsReadContext.xlsReadSheetHolder().setCellExtra(cellExtra);
        xlsReadContext.analysisEventProcessor().extra(xlsReadContext);
    }
}
