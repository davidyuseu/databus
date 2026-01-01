package excel.write.builder;

import excel.ExcelWriter;
import excel.exception.ExcelGenerateException;
import excel.write.metadata.WriteSheet;
import excel.write.metadata.fill.FillConfig;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Build sheet
 *
 * @author Jiaju Zhuang
 */
public class ExcelWriterSheetBuilder extends AbstractExcelWriterParameterBuilder<excel.write.builder.ExcelWriterSheetBuilder, WriteSheet> {
    private ExcelWriter excelWriter;
    /**
     * Sheet
     */
    private WriteSheet writeSheet;

    public ExcelWriterSheetBuilder() {
        this.writeSheet = new WriteSheet();
    }

    public ExcelWriterSheetBuilder(ExcelWriter excelWriter) {
        this.writeSheet = new WriteSheet();
        this.excelWriter = excelWriter;
    }

    /**
     * Starting from 0
     *
     * @param sheetNo
     * @return
     */
    public excel.write.builder.ExcelWriterSheetBuilder sheetNo(Integer sheetNo) {
        writeSheet.setSheetNo(sheetNo);
        return this;
    }

    /**
     * sheet name
     *
     * @param sheetName
     * @return
     */
    public excel.write.builder.ExcelWriterSheetBuilder sheetName(String sheetName) {
        writeSheet.setSheetName(sheetName);
        return this;
    }

    public WriteSheet build() {
        return writeSheet;
    }

    public void doWrite(Collection<?> data) {
        if (excelWriter == null) {
            throw new ExcelGenerateException("Must use 'EasyExcelFactory.write().sheet()' to call this method");
        }
        excelWriter.write(data, build());
        excelWriter.finish();
    }

    public void doFill(Object data) {
        doFill(data, null);
    }

    public void doFill(Object data, FillConfig fillConfig) {
        if (excelWriter == null) {
            throw new ExcelGenerateException("Must use 'EasyExcelFactory.write().sheet()' to call this method");
        }
        excelWriter.fill(data, fillConfig, build());
        excelWriter.finish();
    }

    public void doWrite(Supplier<Collection<?>> supplier) {
        doWrite(supplier.get());
    }

    public void doFill(Supplier<Object> supplier) {
        doFill(supplier.get());
    }

    public void doFill(Supplier<Object> supplier, FillConfig fillConfig) {
        doFill(supplier.get(), fillConfig);
    }

    public ExcelWriterTableBuilder table() {
        return table(null);
    }

    public ExcelWriterTableBuilder table(Integer tableNo) {
        ExcelWriterTableBuilder excelWriterTableBuilder = new ExcelWriterTableBuilder(excelWriter, build());
        if (tableNo != null) {
            excelWriterTableBuilder.tableNo(tableNo);
        }
        return excelWriterTableBuilder;
    }

    @Override
    protected WriteSheet parameter() {
        return writeSheet;
    }

}
