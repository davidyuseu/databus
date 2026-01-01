package excel.converters.bigdecimal;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;

/**
 * BigDecimal and boolean converter
 *
 * @author Jiaju Zhuang
 */
public class BigDecimalBooleanConverter implements Converter<BigDecimal> {

    @Override
    public Class<BigDecimal> supportJavaTypeKey() {
        return BigDecimal.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.BOOLEAN;
    }

    @Override
    public BigDecimal convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (cellData.getBooleanValue()) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public WriteCellData<?> convertToExcelData(BigDecimal value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (BigDecimal.ONE.equals(value)) {
            return new WriteCellData<>(Boolean.TRUE);
        }
        return new WriteCellData<>(Boolean.FALSE);
    }

}
