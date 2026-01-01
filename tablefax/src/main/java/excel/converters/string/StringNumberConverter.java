package excel.converters.string;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.DateUtils;
import excel.util.NumberDataFormatterUtils;
import excel.util.NumberUtils;
import excel.util.StringUtils;
import org.apache.poi.ss.usermodel.DateUtil;

import java.math.BigDecimal;

/**
 * String and number converter
 *
 * @author Jiaju Zhuang
 */
public class StringNumberConverter implements Converter<String> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        // If there are "DateTimeFormat", read as date
        if (contentProperty != null && contentProperty.getDateTimeFormatProperty() != null) {
            return DateUtils.format(
                DateUtil.getJavaDate(cellData.getNumberValue().doubleValue(),
                    contentProperty.getDateTimeFormatProperty().getUse1904windowing(), null),
                contentProperty.getDateTimeFormatProperty().getFormat());
        }
        // If there are "NumberFormat", read as number
        if (contentProperty != null && contentProperty.getNumberFormatProperty() != null) {
            return NumberUtils.format(cellData.getNumberValue(), contentProperty);
        }
        // Excel defines formatting
        boolean hasDataFormatData = cellData.getDataFormatData() != null
            && cellData.getDataFormatData().getIndex() != null && !StringUtils.isEmpty(
            cellData.getDataFormatData().getFormat());
        if (hasDataFormatData) {
            return NumberDataFormatterUtils.format(cellData.getNumberValue(),
                cellData.getDataFormatData().getIndex(), cellData.getDataFormatData().getFormat(), globalConfiguration);
        }
        // Default conversion number
        return NumberUtils.format(cellData.getNumberValue(), contentProperty);
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(new BigDecimal(value));
    }
}
