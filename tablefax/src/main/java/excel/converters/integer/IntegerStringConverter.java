package excel.converters.integer;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

import java.text.ParseException;

/**
 * Integer and string converter
 *
 * @author Jiaju Zhuang
 */
public class IntegerStringConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) throws ParseException {
        return NumberUtils.parseInteger(cellData.getStringValue(), contentProperty);
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return NumberUtils.formatToCellDataString(value, contentProperty);
    }
}
