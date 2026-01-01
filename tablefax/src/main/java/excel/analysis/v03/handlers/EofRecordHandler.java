package excel.analysis.v03.handlers;

import excel.analysis.v03.IgnorableXlsRecordHandler;
import excel.context.xls.XlsReadContext;
import excel.enums.RowTypeEnum;
import excel.metadata.Cell;
import excel.read.metadata.holder.ReadRowHolder;
import excel.read.metadata.holder.xls.XlsReadSheetHolder;
import org.apache.poi.hssf.record.Record;
import java.util.LinkedHashMap;

/**
 * Record handler
 *
 * @author Dan Zheng
 */
public class EofRecordHandler extends AbstractXlsRecordHandler implements IgnorableXlsRecordHandler {

    @Override
    public void processRecord(XlsReadContext xlsReadContext, Record record) {
        if (xlsReadContext.readSheetHolder() == null) {
            return;
        }
        // Sometimes tables lack the end record of the last column
        if (!xlsReadContext.xlsReadSheetHolder().getCellMap().isEmpty()) {
            XlsReadSheetHolder xlsReadSheetHolder = xlsReadContext.xlsReadSheetHolder();
            // Forge a termination data
            xlsReadContext.readRowHolder(new ReadRowHolder(xlsReadContext.xlsReadSheetHolder().getRowIndex() + 1,
                xlsReadSheetHolder.getTempRowType(),
                xlsReadContext.readSheetHolder().getGlobalConfiguration(), xlsReadSheetHolder.getCellMap()));
            xlsReadContext.analysisEventProcessor().endRow(xlsReadContext);
            xlsReadSheetHolder.setCellMap(new LinkedHashMap<Integer, Cell>());
            xlsReadSheetHolder.setTempRowType(RowTypeEnum.EMPTY);
        }

        xlsReadContext.analysisEventProcessor().endSheet(xlsReadContext);
    }
}
