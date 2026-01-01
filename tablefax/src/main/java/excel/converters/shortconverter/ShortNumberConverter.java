package excel.converters.shortconverter;

import excel.converters.Converter;
import excel.converters.WriteConverterContext;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

/**
 * Short and number converter
 *
 * @author Jiaju Zhuang
 */
public class ShortNumberConverter implements Converter<Short> {

    @Override
    public Class<Short> supportJavaTypeKey() {
        return Short.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Short convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue().shortValue();
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Short> context) {
        return NumberUtils.formatToCellData(context.getValue(), context.getContentProperty());
    }
}
