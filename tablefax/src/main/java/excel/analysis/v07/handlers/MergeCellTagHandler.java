package excel.analysis.v07.handlers;

import excel.constant.ExcelXmlConstants;
import excel.context.xlsx.XlsxReadContext;
import excel.enums.CellExtraTypeEnum;
import excel.metadata.CellExtra;
import excel.util.StringUtils;
import org.xml.sax.Attributes;

/**
 * Cell Handler
 *
 * @author Jiaju Zhuang
 */
public class MergeCellTagHandler extends AbstractXlsxTagHandler {

    @Override
    public boolean support(XlsxReadContext xlsxReadContext) {
        return xlsxReadContext.readWorkbookHolder().getExtraReadSet().contains(CellExtraTypeEnum.MERGE);
    }

    @Override
    public void startElement(XlsxReadContext xlsxReadContext, String name, Attributes attributes) {
        String ref = attributes.getValue(ExcelXmlConstants.ATTRIBUTE_REF);
        if (StringUtils.isEmpty(ref)) {
            return;
        }
        CellExtra cellExtra = new CellExtra(CellExtraTypeEnum.MERGE, null, ref);
        xlsxReadContext.readSheetHolder().setCellExtra(cellExtra);
        xlsxReadContext.analysisEventProcessor().extra(xlsxReadContext);
    }

}
