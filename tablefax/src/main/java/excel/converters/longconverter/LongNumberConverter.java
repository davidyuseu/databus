package excel.converters.longconverter;

import excel.converters.Converter;
import excel.converters.WriteConverterContext;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

/**
 * Long and number converter
 *
 * @author Jiaju Zhuang
 */
public class LongNumberConverter implements Converter<Long> {

    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue().longValue();
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Long> context) {
        return NumberUtils.formatToCellData(context.getValue(), context.getContentProperty());
    }

}
