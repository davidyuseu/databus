package excel.context.xls;

import excel.context.AnalysisContextImpl;
import excel.read.metadata.ReadWorkbook;
import excel.read.metadata.holder.xls.XlsReadSheetHolder;
import excel.read.metadata.holder.xls.XlsReadWorkbookHolder;
import excel.support.ExcelTypeEnum;

/**
 *
 * A context is the main anchorage point of a ls xls reader.
 *
 * @author Jiaju Zhuang
 */
public class DefaultXlsReadContext extends AnalysisContextImpl implements XlsReadContext {

    public DefaultXlsReadContext(ReadWorkbook readWorkbook, ExcelTypeEnum actualExcelType) {
        super(readWorkbook, actualExcelType);
    }

    @Override
    public XlsReadWorkbookHolder xlsReadWorkbookHolder() {
        return (XlsReadWorkbookHolder)readWorkbookHolder();
    }

    @Override
    public XlsReadSheetHolder xlsReadSheetHolder() {
        return (XlsReadSheetHolder)readSheetHolder();
    }
}
