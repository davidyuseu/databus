package excel.event;

import excel.context.AnalysisContext;
import excel.metadata.data.ReadCellData;
import excel.read.listener.ReadListener;
import excel.util.ConverterUtils;

import java.util.Map;

/**
 * Receives the return of each piece of data parsed
 *
 * @author jipengfei
 */
public abstract class AnalysisEventListener<T> implements ReadListener<T> {

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        invokeHeadMap(ConverterUtils.convertToStringMap(headMap, context), context);
    }

    /**
     * Returns the header as a map.Override the current method to receive header data.
     *
     * @param headMap
     * @param context
     */
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {}

}
