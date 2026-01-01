package excel.write.style.column;

import excel.metadata.Head;
import excel.metadata.data.WriteCellData;
import excel.write.handler.CellWriteHandler;
import excel.write.handler.context.CellWriteHandlerContext;
import excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;

/**
 * Column width style strategy
 *
 * @author Jiaju Zhuang
 */
public abstract class AbstractColumnWidthStyleStrategy implements CellWriteHandler {

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        setColumnWidth(context);
    }

    /**
     * Sets the column width when head create
     *
     * @param context
     */
    protected void setColumnWidth(CellWriteHandlerContext context) {
        setColumnWidth(context.getWriteSheetHolder(), context.getCellDataList(), context.getCell(),
            context.getHeadData(), context.getRelativeRowIndex(), context.getHead());
    }

    /**
     * Sets the column width when head create
     *
     * @param writeSheetHolder
     * @param cellDataList
     * @param cell
     * @param head
     * @param relativeRowIndex
     * @param isHead
     */
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList, Cell cell,
        Head head, Integer relativeRowIndex, Boolean isHead) {
        throw new UnsupportedOperationException("Custom styles must override the setColumnWidth method.");
    }

}
