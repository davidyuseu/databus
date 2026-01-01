package excel.converters.date;

import excel.converters.Converter;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.DateUtils;
import excel.util.WorkBookUtil;

import java.util.Date;

/**
 * Date and date converter
 *
 * @author Jiaju Zhuang
 */
public class DateDateConverter implements Converter<Date> {
    @Override
    public Class<Date> supportJavaTypeKey() {
        return Date.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Date value, ExcelContentProperty contentProperty,
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
