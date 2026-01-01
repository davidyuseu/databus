package excel.converters;

import excel.context.WriteContext;
import excel.metadata.property.ExcelContentProperty;
import lombok.*;

/**
 * write converter context
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WriteConverterContext<T> {

    /**
     * Java Data.NotNull.
     */
    private T value;

    /**
     * Content property.Nullable.
     */
    private ExcelContentProperty contentProperty;

    /**
     * write context
     */
    private WriteContext writeContext;
}
