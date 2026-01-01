package excel.read.metadata.holder.xlsx;

import excel.constant.BuiltinFormats;
import excel.metadata.data.DataFormatData;
import excel.read.metadata.ReadWorkbook;
import excel.read.metadata.holder.ReadWorkbookHolder;
import excel.support.ExcelTypeEnum;
import excel.util.MapUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import javax.xml.parsers.SAXParserFactory;
import java.util.Map;

/**
 * Workbook holder
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class XlsxReadWorkbookHolder extends ReadWorkbookHolder {
    /**
     * Package
     */
    private OPCPackage opcPackage;
    /**
     * SAXParserFactory used when reading xlsx.
     * <p>
     * The default will automatically find.
     * <p>
     * Please pass in the name of a class ,like : "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"
     *
     * @see SAXParserFactory#newInstance()
     * @see SAXParserFactory#newInstance(String, ClassLoader)
     */
    private String saxParserFactoryName;
    /**
     * Current style information
     */
    private StylesTable stylesTable;
    /**
     * cache data format
     */
    private Map<Integer, DataFormatData> dataFormatDataCache;

    public XlsxReadWorkbookHolder(ReadWorkbook readWorkbook) {
        super(readWorkbook);
        this.saxParserFactoryName = readWorkbook.getXlsxSAXParserFactoryName();
        setExcelType(ExcelTypeEnum.XLSX);
        dataFormatDataCache = MapUtils.newHashMap();
    }

    public DataFormatData dataFormatData(int dateFormatIndexInteger) {
        return dataFormatDataCache.computeIfAbsent(dateFormatIndexInteger, key -> {
            DataFormatData dataFormatData = new DataFormatData();
            if (stylesTable == null) {
                return null;
            }
            XSSFCellStyle xssfCellStyle = stylesTable.getStyleAt(dateFormatIndexInteger);
            dataFormatData.setIndex(xssfCellStyle.getDataFormat());
            dataFormatData.setFormat(BuiltinFormats.getBuiltinFormat(dataFormatData.getIndex(),
                xssfCellStyle.getDataFormatString(), globalConfiguration().getLocale()));
            return dataFormatData;
        });
    }

}
