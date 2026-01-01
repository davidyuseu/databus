package excel.converters.biginteger;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.NumberUtils;

import java.math.BigInteger;

/**
 * BigInteger and number converter
 *
 * @author Jiaju Zhuang
 */
public class BigIntegerNumberConverter implements Converter<BigInteger> {

    @Override
    public Class<BigInteger> supportJavaTypeKey() {
        return BigInteger.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public BigInteger convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return cellData.getNumberValue().toBigInteger();
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigInteger value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return NumberUtils.formatToCellData(value, contentProperty);
    }
}
