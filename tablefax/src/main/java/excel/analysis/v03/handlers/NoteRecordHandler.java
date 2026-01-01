package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.enums.CellExtraTypeEnum;
import excel.metadata.CellExtra;
import org.apache.poi.hssf.record.NoteRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class NoteRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public boolean support(XlsReadContext xlsReadContext, Record record) {
        return xlsReadContext.readWorkbookHolder().getExtraReadSet().contains(CellExtraTypeEnum.COMMENT);
    }

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        NoteRecord nr = (NoteRecord)record;
        String text = xlsReadContext.xlsReadSheetHolder().getObjectCacheMap().get(nr.getShapeId());
        CellExtra cellExtra = new CellExtra(CellExtraTypeEnum.COMMENT, text, nr.getRow(), nr.getColumn());
        xlsReadContext.xlsReadSheetHolder().setCellExtra(cellExtra);
        xlsReadContext.analysisEventProcessor().extra(xlsReadContext);
    }
}
