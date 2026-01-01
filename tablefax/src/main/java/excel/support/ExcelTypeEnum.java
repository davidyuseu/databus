package excel.support;

import excel.exception.ExcelAnalysisException;
import excel.exception.ExcelCommonException;
import excel.read.metadata.ReadWorkbook;
import excel.util.StringUtils;
import org.apache.poi.poifs.filesystem.FileMagic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author jipengfei
 */
public enum ExcelTypeEnum {
    /**
     * csv
     */
    CSV(".csv"),
    /**
     * xls
     */
    XLS(".xls"),
    /**
     * xlsx
     */
    XLSX(".xlsx");

    private String value;

    ExcelTypeEnum(String value) {
        this.setValue(value);
    }

    public static excel.support.ExcelTypeEnum valueOf(ReadWorkbook readWorkbook) {
        excel.support.ExcelTypeEnum excelType = readWorkbook.getExcelType();
        if (excelType != null) {
            return excelType;
        }
        File file = readWorkbook.getFile();
        InputStream inputStream = readWorkbook.getInputStream();
        if (file == null && inputStream == null) {
            throw new ExcelAnalysisException("File and inputStream must be a non-null.");
        }
        try {
            if (file != null) {
                if (!file.exists()) {
                    throw new ExcelAnalysisException("File " + file.getAbsolutePath() + " not exists.");
                }
                // If there is a password, use the FileMagic first
                if (!StringUtils.isEmpty(readWorkbook.getPassword())) {
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                        return recognitionExcelType(bufferedInputStream);
                    }
                }
                // Use the name to determine the type
                String fileName = file.getName();
                if (fileName.endsWith(XLSX.getValue())) {
                    return XLSX;
                } else if (fileName.endsWith(XLS.getValue())) {
                    return XLS;
                } else if (fileName.endsWith(CSV.getValue())) {
                    return CSV;
                }
                if (StringUtils.isEmpty(readWorkbook.getPassword())) {
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
                        return recognitionExcelType(bufferedInputStream);
                    }
                }
            }
            if (!inputStream.markSupported()) {
                inputStream = new BufferedInputStream(inputStream);
                readWorkbook.setInputStream(inputStream);
            }
            return recognitionExcelType(inputStream);
        } catch (ExcelCommonException e) {
            throw e;
        } catch (Exception e) {
            throw new ExcelCommonException(
                "Convert excel format exception.You can try specifying the 'excelType' yourself", e);
        }
    }

    private static excel.support.ExcelTypeEnum recognitionExcelType(InputStream inputStream) throws Exception {
        FileMagic fileMagic = FileMagic.valueOf(inputStream);
        if (FileMagic.OLE2.equals(fileMagic)) {
            return XLS;
        }
        if (FileMagic.OOXML.equals(fileMagic)) {
            return XLSX;
        }
        throw new ExcelCommonException(
            "Convert excel format exception.You can try specifying the 'excelType' yourself");
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
