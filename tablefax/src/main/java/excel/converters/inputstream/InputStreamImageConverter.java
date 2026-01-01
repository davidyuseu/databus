package excel.converters.inputstream;

import excel.converters.Converter;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.IoUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * File and image converter
 *
 * @author Jiaju Zhuang
 */
public class InputStreamImageConverter implements Converter<InputStream> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return InputStream.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(InputStream value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) throws IOException {
        return new WriteCellData<>(IoUtils.toByteArray(value));
    }

}
