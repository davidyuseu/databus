package excel.write.builder;

import excel.ExcelWriter;
import excel.exception.ExcelGenerateException;
import excel.write.metadata.WriteSheet;
import excel.write.metadata.WriteTable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Build sheet
 *
 * @author Jiaju Zhuang
 */
public class ExcelWriterTableBuilder extends AbstractExcelWriterParameterBuilder<excel.write.builder.ExcelWriterTableBuilder, WriteTable> {

    private ExcelWriter excelWriter;

    private WriteSheet writeSheet;
    /**
     * table
     */
    private WriteTable writeTable;

    public ExcelWriterTableBuilder() {
        this.writeTable = new WriteTable();
    }

    public ExcelWriterTableBuilder(ExcelWriter excelWriter, WriteSheet writeSheet) {
        this.excelWriter = excelWriter;
        this.writeSheet = writeSheet;
        this.writeTable = new WriteTable();
    }

    /**
     * Starting from 0
     *
     * @param tableNo
     * @return
     */
    public excel.write.builder.ExcelWriterTableBuilder tableNo(Integer tableNo) {
        writeTable.setTableNo(tableNo);
        return this;
    }

    public WriteTable build() {
        return writeTable;
    }

    public void doWrite(Collection<?> data) {
        if (excelWriter == null) {
            throw new ExcelGenerateException("Must use 'EasyExcelFactory.write().sheet().table()' to call this method");
        }
        excelWriter.write(data, writeSheet, build());
        excelWriter.finish();
    }

    public void doWrite(Supplier<Collection<?>> supplier) {
        doWrite(supplier.get());
    }

    @Override
    protected WriteTable parameter() {
        return writeTable;
    }
}
