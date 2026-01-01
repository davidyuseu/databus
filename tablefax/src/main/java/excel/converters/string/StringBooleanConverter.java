package excel.converters.string;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;

/**
 * String and boolean converter
 *
 * @author Jiaju Zhuang
 */
public class StringBooleanConverter implements Converter<String> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.BOOLEAN;
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getBooleanValue().toString();
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(Boolean.valueOf(value));
    }

}
