package excel.metadata.property;

import excel.converters.Converter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

/**
 * @author jipengfei
 */
@Getter
@Setter
@EqualsAndHashCode
public class ExcelContentProperty {
    public static final excel.metadata.property.ExcelContentProperty EMPTY = new excel.metadata.property.ExcelContentProperty();

    /**
     * Java filed
     */
    private Field field;
    /**
     * Custom defined converters
     */
    private Converter<?> converter;
    /**
     * date time format
     */
    private DateTimeFormatProperty dateTimeFormatProperty;
    /**
     * number format
     */
    private NumberFormatProperty numberFormatProperty;
    /**
     * Content style
     */
    private StyleProperty contentStyleProperty;
    /**
     * Content font
     */
    private FontProperty contentFontProperty;
}
