package excel.analysis.v03;

import excel.analysis.ExcelReadExecutor;
import excel.analysis.v03.handlers.*;
import excel.context.xls.XlsReadContext;
import excel.exception.ExcelAnalysisException;
import excel.exception.ExcelAnalysisStopException;
import excel.read.metadata.ReadSheet;
import excel.read.metadata.holder.xls.XlsReadWorkbookHolder;
import org.apache.poi.hssf.eventusermodel.*;
import org.apache.poi.hssf.record.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.record.Record;
/**
 * /** * A text extractor for Excel files. *
 * <p>
 * * Returns the textual content of the file, suitable for * indexing by something like Lucene, but not really *
 * intended for display to the user. *
 * </p>
 * *
 * <p>
 * * To turn an excel file into a CSV or similar, then see * the XLS2CSVmra example *
 * </p>
 * * * @see <a href= "http://svn.apache.org/repos/asf/poi/trunk/src/examples/src/org/apache/poi/hssf/eventusermodel/examples/XLS2CSVmra.java">XLS2CSVmra</a>
 *
 * @author jipengfei
 */
public class XlsSaxAnalyser implements HSSFListener, ExcelReadExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(excel.analysis.v03.XlsSaxAnalyser.class);
    private static final short DUMMY_RECORD_SID = -1;
    private XlsReadContext xlsReadContext;
    private static final Map<Short, XlsRecordHandler> XLS_RECORD_HANDLER_MAP = new HashMap<Short, XlsRecordHandler>(32);

    static {
        XLS_RECORD_HANDLER_MAP.put(BlankRecord.sid, new BlankRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(BOFRecord.sid, new BofRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(BoolErrRecord.sid, new BoolErrRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(BoundSheetRecord.sid, new BoundSheetRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(DUMMY_RECORD_SID, new DummyRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(EOFRecord.sid, new EofRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(FormulaRecord.sid, new FormulaRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(HyperlinkRecord.sid, new HyperlinkRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(IndexRecord.sid, new IndexRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(LabelRecord.sid, new LabelRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(LabelSSTRecord.sid, new LabelSstRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(MergeCellsRecord.sid, new MergeCellsRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(NoteRecord.sid, new NoteRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(NumberRecord.sid, new NumberRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(ObjRecord.sid, new ObjRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(RKRecord.sid, new RkRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(SSTRecord.sid, new SstRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(StringRecord.sid, new StringRecordHandler());
        XLS_RECORD_HANDLER_MAP.put(TextObjectRecord.sid, new TextObjectRecordHandler());
    }

    public XlsSaxAnalyser(XlsReadContext xlsReadContext) {
        this.xlsReadContext = xlsReadContext;
    }

    @Override
    public List<ReadSheet> sheetList() {
        try {
            if (xlsReadContext.readWorkbookHolder().getActualSheetDataList() == null) {
                new XlsListSheetListener(xlsReadContext).execute();
            }
        } catch (ExcelAnalysisStopException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Custom stop!");
            }
        }
        return xlsReadContext.readWorkbookHolder().getActualSheetDataList();
    }

    @Override
    public void execute() {
        XlsReadWorkbookHolder xlsReadWorkbookHolder = xlsReadContext.xlsReadWorkbookHolder();
        MissingRecordAwareHSSFListener listener = new MissingRecordAwareHSSFListener(this);
        xlsReadWorkbookHolder.setFormatTrackingHSSFListener(new FormatTrackingHSSFListener(listener));
        EventWorkbookBuilder.SheetRecordCollectingListener workbookBuildingListener =
            new EventWorkbookBuilder.SheetRecordCollectingListener(
                xlsReadWorkbookHolder.getFormatTrackingHSSFListener());
        xlsReadWorkbookHolder.setHssfWorkbook(workbookBuildingListener.getStubHSSFWorkbook());
        HSSFEventFactory factory = new HSSFEventFactory();
        HSSFRequest request = new HSSFRequest();
        request.addListenerForAllRecords(xlsReadWorkbookHolder.getFormatTrackingHSSFListener());
        try {
            factory.processWorkbookEvents(request, xlsReadWorkbookHolder.getPoifsFileSystem());
        } catch (IOException e) {
            throw new ExcelAnalysisException(e);
        }
    }

    @Override
    public void processRecord(Record record) {
        XlsRecordHandler handler = XLS_RECORD_HANDLER_MAP.get(record.getSid());
        if (handler == null) {
            return;
        }
        boolean ignoreRecord =
            (handler instanceof IgnorableXlsRecordHandler) && xlsReadContext.xlsReadWorkbookHolder().getIgnoreRecord();
        if (ignoreRecord) {
            // No need to read the current sheet
            return;
        }
        if (!handler.support(xlsReadContext, record)) {
            return;
        }
        handler.processRecord(xlsReadContext, record);
    }

}
