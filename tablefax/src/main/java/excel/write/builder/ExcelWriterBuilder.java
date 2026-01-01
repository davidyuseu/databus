package excel.write.builder;

import excel.ExcelWriter;
import excel.support.ExcelTypeEnum;
import excel.write.metadata.WriteWorkbook;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Build ExcelBuilder
 *
 * @author Jiaju Zhuang
 */
public class ExcelWriterBuilder extends AbstractExcelWriterParameterBuilder<excel.write.builder.ExcelWriterBuilder, WriteWorkbook> {
    /**
     * Workbook
     */
    private final WriteWorkbook writeWorkbook;

    public ExcelWriterBuilder() {
        this.writeWorkbook = new WriteWorkbook();
    }

    /**
     * Default true
     *
     * @param autoCloseStream
     * @return
     */
    public excel.write.builder.ExcelWriterBuilder autoCloseStream(Boolean autoCloseStream) {
        writeWorkbook.setAutoCloseStream(autoCloseStream);
        return this;
    }

    /**
     * Whether the encryption.
     * <p>
     * WARRING:Encryption is when the entire file is read into memory, so it is very memory intensive.
     *
     * @param password
     * @return
     */
    public excel.write.builder.ExcelWriterBuilder password(String password) {
        writeWorkbook.setPassword(password);
        return this;
    }

    /**
     * Write excel in memory. Default false, the cache file is created and finally written to excel.
     * <p>
     * Comment and RichTextString are only supported in memory mode.
     */
    public excel.write.builder.ExcelWriterBuilder inMemory(Boolean inMemory) {
        writeWorkbook.setInMemory(inMemory);
        return this;
    }

    /**
     * Excel is also written in the event of an exception being thrown.The default false.
     */
    public excel.write.builder.ExcelWriterBuilder writeExcelOnException(Boolean writeExcelOnException) {
        writeWorkbook.setWriteExcelOnException(writeExcelOnException);
        return this;
    }


    public excel.write.builder.ExcelWriterBuilder excelType(ExcelTypeEnum excelType) {
        writeWorkbook.setExcelType(excelType);
        return this;
    }

    public excel.write.builder.ExcelWriterBuilder file(OutputStream outputStream) {
        writeWorkbook.setOutputStream(outputStream);
        return this;
    }

    public excel.write.builder.ExcelWriterBuilder file(File outputFile) {
        writeWorkbook.setFile(outputFile);
        return this;
    }

    public excel.write.builder.ExcelWriterBuilder file(String outputPathName) {
        return file(new File(outputPathName));
    }

    public excel.write.builder.ExcelWriterBuilder withTemplate(InputStream templateInputStream) {
        writeWorkbook.setTemplateInputStream(templateInputStream);
        return this;
    }

    public excel.write.builder.ExcelWriterBuilder withTemplate(File templateFile) {
        writeWorkbook.setTemplateFile(templateFile);
        return this;
    }

    public excel.write.builder.ExcelWriterBuilder withTemplate(String pathName) {
        return withTemplate(new File(pathName));
    }


    public ExcelWriter build() {
        return new ExcelWriter(writeWorkbook);
    }

    public ExcelWriterSheetBuilder sheet() {
        return sheet(null, null);
    }

    public ExcelWriterSheetBuilder sheet(Integer sheetNo) {
        return sheet(sheetNo, null);
    }

    public ExcelWriterSheetBuilder sheet(String sheetName) {
        return sheet(null, sheetName);
    }

    public ExcelWriterSheetBuilder sheet(Integer sheetNo, String sheetName) {
        ExcelWriter excelWriter = build();
        ExcelWriterSheetBuilder excelWriterSheetBuilder = new ExcelWriterSheetBuilder(excelWriter);
        if (sheetNo != null) {
            excelWriterSheetBuilder.sheetNo(sheetNo);
        }
        if (sheetName != null) {
            excelWriterSheetBuilder.sheetName(sheetName);
        }
        return excelWriterSheetBuilder;
    }

    @Override
    protected WriteWorkbook parameter() {
        return writeWorkbook;
    }
}
