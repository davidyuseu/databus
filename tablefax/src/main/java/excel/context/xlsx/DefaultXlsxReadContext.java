package excel.context.xlsx;

import excel.context.AnalysisContextImpl;
import excel.read.metadata.ReadWorkbook;
import excel.read.metadata.holder.xlsx.XlsxReadSheetHolder;
import excel.read.metadata.holder.xlsx.XlsxReadWorkbookHolder;
import excel.support.ExcelTypeEnum;

/**
 *
 * A context is the main anchorage point of a ls xls reader.
 *
 * @author Jiaju Zhuang
 */
public class DefaultXlsxReadContext extends AnalysisContextImpl implements XlsxReadContext {

    public DefaultXlsxReadContext(ReadWorkbook readWorkbook, ExcelTypeEnum actualExcelType) {
        super(readWorkbook, actualExcelType);
    }

    @Override
    public XlsxReadWorkbookHolder xlsxReadWorkbookHolder() {
        return (XlsxReadWorkbookHolder)readWorkbookHolder();
    }

    @Override
    public XlsxReadSheetHolder xlsxReadSheetHolder() {
        return (XlsxReadSheetHolder)readSheetHolder();
    }
}
