package excel.analysis.v07.handlers;

import excel.constant.ExcelXmlConstants;
import excel.context.xlsx.XlsxReadContext;
import excel.enums.RowTypeEnum;
import excel.metadata.Cell;
import excel.read.metadata.holder.ReadRowHolder;
import excel.read.metadata.holder.xlsx.XlsxReadSheetHolder;
import excel.util.PositionUtils;
import org.apache.commons.collections4.MapUtils;
import org.xml.sax.Attributes;

import java.util.LinkedHashMap;

/**
 * Cell Handler
 *
 * @author jipengfei
 */
public class RowTagHandler extends AbstractXlsxTagHandler {

    @Override
    public void startElement(XlsxReadContext xlsxReadContext, String name, Attributes attributes) {
        XlsxReadSheetHolder xlsxReadSheetHolder = xlsxReadContext.xlsxReadSheetHolder();
        int rowIndex = PositionUtils.getRowByRowTagt(attributes.getValue(ExcelXmlConstants.ATTRIBUTE_R),
            xlsxReadSheetHolder.getRowIndex());
        Integer lastRowIndex = xlsxReadContext.readSheetHolder().getRowIndex();
        while (lastRowIndex + 1 < rowIndex) {
            xlsxReadContext.readRowHolder(new ReadRowHolder(lastRowIndex + 1, RowTypeEnum.EMPTY,
                xlsxReadSheetHolder.getGlobalConfiguration(), new LinkedHashMap<Integer, Cell>()));
            xlsxReadContext.analysisEventProcessor().endRow(xlsxReadContext);
            xlsxReadSheetHolder.setColumnIndex(null);
            xlsxReadSheetHolder.setCellMap(new LinkedHashMap<Integer, Cell>());
            lastRowIndex++;
        }
        xlsxReadSheetHolder.setRowIndex(rowIndex);
    }

    @Override
    public void endElement(XlsxReadContext xlsxReadContext, String name) {
        XlsxReadSheetHolder xlsxReadSheetHolder = xlsxReadContext.xlsxReadSheetHolder();
        RowTypeEnum rowType = MapUtils.isEmpty(xlsxReadSheetHolder.getCellMap()) ? RowTypeEnum.EMPTY : RowTypeEnum.DATA;
        xlsxReadContext.readRowHolder(new ReadRowHolder(xlsxReadSheetHolder.getRowIndex(), rowType,
            xlsxReadSheetHolder.getGlobalConfiguration(), xlsxReadSheetHolder.getCellMap()));
        xlsxReadContext.analysisEventProcessor().endRow(xlsxReadContext);
        xlsxReadSheetHolder.setColumnIndex(null);
        xlsxReadSheetHolder.setCellMap(new LinkedHashMap<>());
    }

}
