package excel.converters.string;

import excel.converters.Converter;
import excel.metadata.GlobalConfiguration;
import excel.metadata.data.WriteCellData;
import excel.metadata.property.ExcelContentProperty;
import excel.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * String and image converter
 *
 * @author Jiaju Zhuang
 */
public class StringImageConverter implements Converter<String> {
    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty,
        GlobalConfiguration globalConfiguration) throws IOException {
        return new WriteCellData<>(FileUtils.readFileToByteArray(new File(value)));
    }

}
