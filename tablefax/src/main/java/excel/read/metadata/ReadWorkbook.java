package excel.read.metadata;

import excel.cache.ReadCache;
import excel.cache.selector.ReadCacheSelector;
import excel.context.AnalysisContext;
import excel.enums.CellExtraTypeEnum;
import excel.event.AnalysisEventListener;
import excel.read.listener.ModelBuildEventListener;
import excel.support.ExcelTypeEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.util.Set;

/**
 * Workbook
 *
 * @author Jiaju Zhuang
 **/
@Getter
@Setter
@EqualsAndHashCode
public class ReadWorkbook extends ReadBasicParameter {
    /**
     * Excel type
     */
    private ExcelTypeEnum excelType;
    /**
     * Read InputStream
     * <p>
     * If 'inputStream' and 'file' all not empty, file first
     */
    private InputStream inputStream;
    /**
     * Read file
     * <p>
     * If 'inputStream' and 'file' all not empty, file first
     */
    private File file;
    /**
     * Mandatory use 'inputStream' .Default is false.
     * <p>
     * if false, Will transfer 'inputStream' to temporary files to improve efficiency
     */
    private Boolean mandatoryUseInputStream;
    /**
     * Default true
     */
    private Boolean autoCloseStream;
    /**
     * This object can be read in the Listener {@link AnalysisEventListener#invoke(Object, AnalysisContext)}
     * {@link AnalysisContext#getCustom()}
     *
     */
    private Object customObject;
    /**
     * A cache that stores temp data to save memory.
     */
    private ReadCache readCache;
    /**
     * Ignore empty rows.Default is true.
     */
    private Boolean ignoreEmptyRow;
    /**
     * Select the cache.Default use {@link excel.cache.selector.SimpleReadCacheSelector}
     */
    private ReadCacheSelector readCacheSelector;
    /**
     * Whether the encryption
     */
    private String password;
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
    private String xlsxSAXParserFactoryName;
    /**
     * Whether to use the default listener, which is used by default.
     * <p>
     * The {@link ModelBuildEventListener} is loaded by default to convert the object.
     */
    private Boolean useDefaultListener;
    /**
     * Read some additional fields. None are read by default.
     *
     * @see CellExtraTypeEnum
     */
    private Set<CellExtraTypeEnum> extraReadSet;
}
