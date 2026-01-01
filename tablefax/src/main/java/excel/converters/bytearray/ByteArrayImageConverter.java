package excel.converters.bytearray;

import excel.converters.Converter;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;

/**
 * Byte array and image converter
 *
 * @author Jiaju Zhuang
 */
public class ByteArrayImageConverter implements Converter<byte[]> {

    @Override
    public Class<byte[]> supportJavaTypeKey() {
        return byte[].class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(byte[] value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) {
        return new WriteCellData<>(value);
    }

}
