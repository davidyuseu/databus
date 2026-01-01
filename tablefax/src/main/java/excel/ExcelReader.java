package excel;

import excel.analysis.ExcelAnalyser;
import excel.analysis.ExcelAnalyserImpl;
import excel.analysis.ExcelReadExecutor;
import excel.context.AnalysisContext;
import excel.read.metadata.ReadSheet;
import excel.read.metadata.ReadWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Excel readers are all read in event mode.
 *
 * @author jipengfei
 */
public class ExcelReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelReader.class);

    /**
     * Analyser
     */
    private ExcelAnalyser excelAnalyser;

    public ExcelReader(ReadWorkbook readWorkbook) {
        excelAnalyser = new ExcelAnalyserImpl(readWorkbook);
    }

    /**
     * Parse all sheet content by default
     *
     * @deprecated lease use {@link #readAll()}
     */
    @Deprecated
    public void read() {
        readAll();
    }

    /***
     * Parse all sheet content by default
     */
    public void readAll() {
        excelAnalyser.analysis(null, Boolean.TRUE);
    }

    /**
     * Parse the specified sheet，SheetNo start from 0
     *
     * @param readSheet
     *            Read sheet
     */
    public excel.ExcelReader read(ReadSheet... readSheet) {
        return read(Arrays.asList(readSheet));
    }

    /**
     * Read multiple sheets.
     *
     * @param readSheetList
     * @return
     */
    public excel.ExcelReader read(List<ReadSheet> readSheetList) {
        excelAnalyser.analysis(readSheetList, Boolean.FALSE);
        return this;
    }


    /**
     * Context for the entire execution process
     *
     * @return
     */
    public AnalysisContext analysisContext() {
        return excelAnalyser.analysisContext();
    }

    /**
     * Current executor
     *
     * @return
     */
    public ExcelReadExecutor excelExecutor() {
        return excelAnalyser.excelExecutor();
    }

    /**
     *
     * @return
     * @deprecated please use {@link #analysisContext()}
     */
    @Deprecated
    public AnalysisContext getAnalysisContext() {
        return analysisContext();
    }

    /**
     * Complete the entire read file.Release the cache and close stream.
     */
    public void finish() {
        if (excelAnalyser != null) {
            excelAnalyser.finish();
        }
    }

    /**
     * Prevents calls to {@link #finish} from freeing the cache
     *
     */
    @Override
    protected void finalize() {
        try {
            finish();
        } catch (Throwable e) {
            LOGGER.warn("Destroy object failed", e);
        }
    }

}
