package excel.metadata.property;

import excel.annotation.format.NumberFormat;

import java.math.RoundingMode;

/**
 * Configuration from annotations
 *
 * @author Jiaju Zhuang
 */
public class NumberFormatProperty {
    private String format;
    private RoundingMode roundingMode;

    public NumberFormatProperty(String format, RoundingMode roundingMode) {
        this.format = format;
        this.roundingMode = roundingMode;
    }

    public static excel.metadata.property.NumberFormatProperty build(NumberFormat numberFormat) {
        if (numberFormat == null) {
            return null;
        }
        return new excel.metadata.property.NumberFormatProperty(numberFormat.value(), numberFormat.roundingMode());
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }
}
