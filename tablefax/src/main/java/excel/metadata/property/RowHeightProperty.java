package excel.metadata.property;

import excel.annotation.write.style.ContentRowHeight;
import excel.annotation.write.style.HeadRowHeight;

/**
 * Configuration from annotations
 *
 * @author Jiaju Zhuang
 */
public class RowHeightProperty {
    private Short height;

    public RowHeightProperty(Short height) {
        this.height = height;
    }

    public static excel.metadata.property.RowHeightProperty build(HeadRowHeight headRowHeight) {
        if (headRowHeight == null || headRowHeight.value() < 0) {
            return null;
        }
        return new excel.metadata.property.RowHeightProperty(headRowHeight.value());
    }

    public static excel.metadata.property.RowHeightProperty build(ContentRowHeight contentRowHeight) {
        if (contentRowHeight == null || contentRowHeight.value() < 0) {
            return null;
        }
        return new excel.metadata.property.RowHeightProperty(contentRowHeight.value());
    }

    public Short getHeight() {
        return height;
    }

    public void setHeight(Short height) {
        this.height = height;
    }
}
