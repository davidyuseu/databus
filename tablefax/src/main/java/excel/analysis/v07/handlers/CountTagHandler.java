package excel.analysis.v07.handlers;

import excel.constant.ExcelXmlConstants;
import excel.context.xlsx.XlsxReadContext;
import excel.util.PositionUtils;
import org.xml.sax.Attributes;

/**
 * Cell Handler
 *
 * @author jipengfei
 */
public class CountTagHandler extends AbstractXlsxTagHandler {

    @Override
    public void startElement(XlsxReadContext xlsxReadContext, String name, Attributes attributes) {
        String d = attributes.getValue(ExcelXmlConstants.ATTRIBUTE_REF);
        String totalStr = d.substring(d.indexOf(":") + 1);
        xlsxReadContext.readSheetHolder().setApproximateTotalRowNumber(PositionUtils.getRow(totalStr) + 1);
    }

}
