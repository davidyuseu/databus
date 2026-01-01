package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.cache.XlsCache;
import excel.context.xls.XlsReadContext;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class SstRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {
    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        xlsReadContext.readWorkbookHolder().setReadCache(new XlsCache((SSTRecord)record));
    }
}
