package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.metadata.data.ReadCellData;
import org.apache.poi.hssf.record.RKRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class RkRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        RKRecord re = (RKRecord)record;
        xlsReadContext.xlsReadSheetHolder().getCellMap().put((int)re.getColumn(),
            ReadCellData.newEmptyInstance(re.getRow(), (int)re.getColumn()));
    }
}
