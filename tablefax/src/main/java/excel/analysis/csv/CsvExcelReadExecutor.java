package excel.analysis.csv;

import excel.analysis.ExcelReadExecutor;
import excel.context.csv.CsvReadContext;
import excel.enums.CellDataTypeEnum;
import excel.enums.RowTypeEnum;
import excel.exception.ExcelAnalysisException;
import excel.metadata.Cell;
import excel.metadata.data.ReadCellData;
import excel.read.metadata.ReadSheet;
import excel.read.metadata.holder.ReadRowHolder;
import excel.read.metadata.holder.csv.CsvReadWorkbookHolder;
import excel.util.SheetUtils;
import excel.util.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * read executor
 *
 * @author zhuangjiaju
 */

public class CsvExcelReadExecutor implements ExcelReadExecutor {

    private List<ReadSheet> sheetList;
    private CsvReadContext csvReadContext;

    public CsvExcelReadExecutor(CsvReadContext csvReadContext) {
        this.csvReadContext = csvReadContext;
        sheetList = new ArrayList<>();
        ReadSheet readSheet = new ReadSheet();
        sheetList.add(readSheet);
        readSheet.setSheetNo(0);
    }

    @Override
    public List<ReadSheet> sheetList() {
        return sheetList;
    }

    @Override
    public void execute() {
        Iterable<CSVRecord> parseRecords;
        try {
            parseRecords = parseRecords();
        } catch (IOException e) {
            throw new ExcelAnalysisException(e);
        }
        for (ReadSheet readSheet : sheetList) {
            readSheet = SheetUtils.match(readSheet, csvReadContext);
            if (readSheet == null) {
                continue;
            }
            csvReadContext.currentSheet(readSheet);

            int rowIndex = 0;

            for (CSVRecord record : parseRecords) {
                dealRecord(record, rowIndex++);
            }

            // The last sheet is read
            csvReadContext.analysisEventProcessor().endSheet(csvReadContext);
        }
    }

    private Iterable<CSVRecord> parseRecords() throws IOException {
        CsvReadWorkbookHolder csvReadWorkbookHolder = csvReadContext.csvReadWorkbookHolder();
        CSVFormat csvFormat = csvReadWorkbookHolder.getCsvFormat();

        if (csvReadWorkbookHolder.getMandatoryUseInputStream()) {
            return csvFormat.parse(new InputStreamReader(csvReadWorkbookHolder.getInputStream()));
        }
        if (csvReadWorkbookHolder.getFile() != null) {
            return csvFormat.parse(new FileReader(csvReadWorkbookHolder.getFile()));
        }
        return csvFormat.parse(new InputStreamReader(csvReadWorkbookHolder.getInputStream()));
    }

    private void dealRecord(CSVRecord record, int rowIndex) {
        Map<Integer, Cell> cellMap = new LinkedHashMap<>();
        Iterator<String> cellIterator = record.iterator();
        int columnIndex = 0;
        while (cellIterator.hasNext()) {
            String cellString = cellIterator.next();
            ReadCellData<String> readCellData = new ReadCellData<>();
            readCellData.setRowIndex(rowIndex);
            readCellData.setColumnIndex(columnIndex);

            // csv is an empty string of whether <code>,,</code> is read or <code>,"",</code>
            if (StringUtils.isNotBlank(cellString)) {
                readCellData.setType(CellDataTypeEnum.STRING);
                readCellData.setStringValue(cellString);
            } else {
                readCellData.setType(CellDataTypeEnum.EMPTY);
            }
            cellMap.put(columnIndex++, readCellData);
        }

        RowTypeEnum rowType = MapUtils.isEmpty(cellMap) ? RowTypeEnum.EMPTY : RowTypeEnum.DATA;
        ReadRowHolder readRowHolder = new ReadRowHolder(rowIndex, rowType,
            csvReadContext.readWorkbookHolder().getGlobalConfiguration(), cellMap);
        csvReadContext.readRowHolder(readRowHolder);

        csvReadContext.csvReadSheetHolder().setCellMap(cellMap);
        csvReadContext.csvReadSheetHolder().setRowIndex(rowIndex);
        csvReadContext.analysisEventProcessor().endRow(csvReadContext);
    }
}
