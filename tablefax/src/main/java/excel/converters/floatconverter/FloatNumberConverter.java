package excel.converters.floatconverter;

import excel.converters.Converter;
import excel.converters.WriteConverterContext;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

/**
 * Float and number converter
 *
 * @author Jiaju Zhuang
 */
public class FloatNumberConverter implements Converter<Float> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Float.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Float convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue().floatValue();
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Float> context) {
        return NumberUtils.formatToCellData(context.getValue(), context.getContentProperty());
    }
}
