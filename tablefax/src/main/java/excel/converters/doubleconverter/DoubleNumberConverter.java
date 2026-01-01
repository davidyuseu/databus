package excel.converters.doubleconverter;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

/**
 * Double and number converter
 *
 * @author Jiaju Zhuang
 */
public class DoubleNumberConverter implements Converter<Double> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Double.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Double convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue().doubleValue();
    }

    @Override
    public WriteCellData<?> convertToExcelData(Double value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return NumberUtils.formatToCellData(value, contentProperty);
    }
}
