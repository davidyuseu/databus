package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import org.apache.poi.hssf.record.IndexRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Jiaju Zhuang
 */
public class IndexRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {
    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        if (xlsReadContext.readSheetHolder() == null) {
            return;
        }
        xlsReadContext.readSheetHolder().setApproximateTotalRowNumber(((IndexRecord)record).getLastRowAdd1());
    }
}
