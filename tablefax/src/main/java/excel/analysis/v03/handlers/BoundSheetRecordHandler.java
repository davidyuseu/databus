package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class BoundSheetRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        BoundSheetRecord bsr = (BoundSheetRecord)record;
        xlsReadContext.xlsReadWorkbookHolder().getBoundSheetRecordList().add((BoundSheetRecord)record);
    }
}
