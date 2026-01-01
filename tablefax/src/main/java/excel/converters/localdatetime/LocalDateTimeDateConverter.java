package excel.converters.localdatetime;

import excel.converters.Converter;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.DateUtils;
import excel.util.WorkBookUtil;

import java.time.LocalDateTime;

/**
 * Date and date converter
 *
 * @author Jiaju Zhuang
 */
public class LocalDateTimeDateConverter implements Converter<LocalDateTime> {
    @Override
    public Class<LocalDateTime> supportJavaTypeKey() {
        return LocalDateTime.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(LocalDateTime value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) throws Exception {
        WriteCellData<?> cellData = new WriteCellData<>(value);
        String format = null;
        if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
            format = contentProperty.getDateTimeFormatProperty().getFormat();
        }
        WorkBookUtil.fillDataFormat(cellData, format, DateUtils.defaultDateFormat);
        return cellData;
    }
}
