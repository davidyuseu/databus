package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.enums.RowTypeEnum;
import excel.metadata.data.ReadCellData;
import org.apache.poi.hssf.record.BoolErrRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class BoolErrRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        BoolErrRecord ber = (BoolErrRecord)record;
        xlsReadContext.xlsReadSheetHolder().getCellMap().put((int)ber.getColumn(),
            ReadCellData.newInstance(ber.getBooleanValue(), ber.getRow(), (int)ber.getColumn()));
        xlsReadContext.xlsReadSheetHolder().setTempRowType(RowTypeEnum.DATA);
    }
}
