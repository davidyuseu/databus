package excel.converters.biginteger;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;

import java.math.BigInteger;

/**
 * BigInteger and boolean converter
 *
 * @author Jiaju Zhuang
 */
public class BigIntegerBooleanConverter implements Converter<BigInteger> {

    @Override
    public Class<BigInteger> supportJavaTypeKey() {
        return BigInteger.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.BOOLEAN;
    }

    @Override
    public BigInteger convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (cellData.getBooleanValue()) {
            return BigInteger.ONE;
        }
        return BigInteger.ZERO;
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigInteger value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (BigInteger.ONE.equals(value)) {
            return new WriteCellData<>(Boolean.TRUE);
        }
        return new WriteCellData<>(Boolean.FALSE);
    }

}
