package excel.converters;

import excel.context.AnalysisContext;
import excel.metadata.data.ReadCellData;
import excel.metadata.property.ExcelContentProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * read converter context
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class ReadConverterContext<T> {
    /**
     * Excel cell data.NotNull.
     */
    private ReadCellData<T> readCellData;
    /**
     * Content property.Nullable.
     */
    private ExcelContentProperty contentProperty;
    /**
     * context.NotNull.
     */
    private AnalysisContext analysisContext;
}
