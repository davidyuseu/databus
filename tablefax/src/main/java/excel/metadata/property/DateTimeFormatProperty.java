package excel.metadata.property;

import excel.annotation.format.DateTimeFormat;
import excel.util.BooleanUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration from annotations
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class DateTimeFormatProperty {
    private String format;
    private Boolean use1904windowing;

    public DateTimeFormatProperty(String format, Boolean use1904windowing) {
        this.format = format;
        this.use1904windowing = use1904windowing;
    }

    public static excel.metadata.property.DateTimeFormatProperty build(DateTimeFormat dateTimeFormat) {
        if (dateTimeFormat == null) {
            return null;
        }
        return new excel.metadata.property.DateTimeFormatProperty(dateTimeFormat.value(),
            BooleanUtils.isTrue(dateTimeFormat.use1904windowing().getBooleanValue()));
    }
}
