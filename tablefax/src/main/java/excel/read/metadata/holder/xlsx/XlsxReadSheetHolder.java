package excel.read.metadata.holder.xlsx;

import excel.read.metadata.ReadSheet;
import excel.read.metadata.holder.ReadSheetHolder;
import excel.read.metadata.holder.ReadWorkbookHolder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.LinkedList;

/**
 * sheet holder
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class XlsxReadSheetHolder extends ReadSheetHolder {
    /**
     * Record the label of the current operation to prevent NPE.
     */
    private Deque<String> tagDeque;
    /**
     * Current Column
     */
    private Integer columnIndex;
    /**
     * Data for current label.
     */
    private StringBuilder tempData;
    /**
     * Formula for current label.
     */
    private StringBuilder tempFormula;

    public XlsxReadSheetHolder(ReadSheet readSheet, ReadWorkbookHolder readWorkbookHolder) {
        super(readSheet, readWorkbookHolder);
        this.tagDeque = new LinkedList<String>();
    }
}
