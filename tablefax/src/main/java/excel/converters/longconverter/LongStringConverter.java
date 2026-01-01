package excel.converters.longconverter;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

import java.text.ParseException;

/**
 * Long and string converter
 *
 * @author Jiaju Zhuang
 */
public class LongStringConverter implements Converter<Long> {

    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) throws ParseException {
        return NumberUtils.parseLong(cellData.getStringValue(), contentProperty);
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return NumberUtils.formatToCellDataString(value, contentProperty);
    }
}
