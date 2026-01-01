package excel.analysis.v03.handlers;

import excel.analysis.v03.XlsRecordHandler;
import excel.context.xls.XlsReadContext;
import org.apache.poi.hssf.record.Record;

/**
 * Abstract xls record handler
 *
 * @author Jiaju Zhuang
 **/
public abstract class AbstractXlsRecordHandler implements XlsRecordHandler {

    @Override
    public boolean support(XlsReadContext xlsReadContext, Record record) {
        return true;
    }
}
