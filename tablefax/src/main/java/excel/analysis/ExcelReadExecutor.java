package excel.analysis;

import excel.read.metadata.ReadSheet;

import java.util.List;

/**
 * Excel file Executor
 *
 * @author Jiaju Zhuang
 */
public interface ExcelReadExecutor {

    /**
     * Returns the actual sheet in excel
     *
     * @return Actual sheet in excel
     */
    List<ReadSheet> sheetList();

    /**
     * Read the sheet.
     */
    void execute();
}
