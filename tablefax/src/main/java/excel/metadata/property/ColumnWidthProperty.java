package excel.metadata.property;

import excel.annotation.write.style.ColumnWidth;

/**
 * Configuration from annotations
 *
 * @author Jiaju Zhuang
 */
public class ColumnWidthProperty {
    private Integer width;

    public ColumnWidthProperty(Integer width) {
        this.width = width;
    }

    public static excel.metadata.property.ColumnWidthProperty build(ColumnWidth columnWidth) {
        if (columnWidth == null || columnWidth.value() < 0) {
            return null;
        }
        return new excel.metadata.property.ColumnWidthProperty(columnWidth.value());
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }
}
