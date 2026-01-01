package excel.converters.byteconverter;

import excel.converters.Converter;
import excel.enums.CellDataTypeEnum;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.ReadCellData;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;

/**
 * Byte and boolean converter
 *
 * @author Jiaju Zhuang
 */
public class ByteBooleanConverter implements Converter<Byte> {
    private static final Byte ONE = (byte)1;
    private static final Byte ZERO = (byte)0;

    @Override
    public Class<?> supportJavaTypeKey() {
        return Byte.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.BOOLEAN;
    }

    @Override
    public Byte convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (cellData.getBooleanValue()) {
            return ONE;
        }
        return ZERO;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Byte value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        if (ONE.equals(value)) {
            return new WriteCellData<>(Boolean.TRUE);
        }
        return new WriteCellData<>(Boolean.FALSE);
    }

}
