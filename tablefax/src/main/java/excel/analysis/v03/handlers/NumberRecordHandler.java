package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.constant.BuiltinFormats;
import excel.context.xls.XlsReadContext;
import excel.enums.RowTypeEnum;
import excel.metadata.data.DataFormatData;
import excel.metadata.data.ReadCellData;
import org.apache.poi.hssf.record.NumberRecord;

import java.math.BigDecimal;
import org.apache.poi.hssf.record.Record;
/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class NumberRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        NumberRecord nr = (NumberRecord)record;
        ReadCellData<?> cellData = ReadCellData.newInstance(BigDecimal.valueOf(nr.getValue()), nr.getRow(),
            (int)nr.getColumn());
        short dataFormat = (short)xlsReadContext.xlsReadWorkbookHolder().getFormatTrackingHSSFListener().getFormatIndex(
            nr);
        DataFormatData dataFormatData = new DataFormatData();
        dataFormatData.setIndex(dataFormat);
        dataFormatData.setFormat(BuiltinFormats.getBuiltinFormat(dataFormat,
            xlsReadContext.xlsReadWorkbookHolder().getFormatTrackingHSSFListener().getFormatString(nr),
            xlsReadContext.readSheetHolder().getGlobalConfiguration().getLocale()));
        cellData.setDataFormatData(dataFormatData);
        xlsReadContext.xlsReadSheetHolder().getCellMap().put((int)nr.getColumn(), cellData);
        xlsReadContext.xlsReadSheetHolder().setTempRowType(RowTypeEnum.DATA);
    }
}
