package excel.metadata.property;

import excel.annotation.write.style.ContentLoopMerge;

/**
 * Configuration from annotations
 *
 * @author Jiaju Zhuang
 */
public class LoopMergeProperty {
    /**
     * Each row
     */
    private int eachRow;
    /**
     * Extend column
     */
    private int columnExtend;

    public LoopMergeProperty(int eachRow, int columnExtend) {
        this.eachRow = eachRow;
        this.columnExtend = columnExtend;
    }

    public static excel.metadata.property.LoopMergeProperty build(ContentLoopMerge contentLoopMerge) {
        if (contentLoopMerge == null) {
            return null;
        }
        return new excel.metadata.property.LoopMergeProperty(contentLoopMerge.eachRow(), contentLoopMerge.columnExtend());
    }

    public int getEachRow() {
        return eachRow;
    }

    public void setEachRow(int eachRow) {
        this.eachRow = eachRow;
    }

    public int getColumnExtend() {
        return columnExtend;
    }

    public void setColumnExtend(int columnExtend) {
        this.columnExtend = columnExtend;
    }
}
