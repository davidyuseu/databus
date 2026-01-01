package excel.read.metadata.holder;

import excel.converters.Converter;
import excel.converters.ConverterKeyBuild;
import excel.converters.DefaultConverterLoader;
import excel.enums.HolderEnum;
import excel.metadata.AbstractHolder;
import excel.read.listener.ModelBuildEventListener;
import excel.read.listener.ReadListener;
import excel.read.metadata.ReadBasicParameter;
import excel.read.metadata.ReadWorkbook;
import excel.read.metadata.property.ExcelReadHeadProperty;
import excel.util.ListUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

/**
 * Read Holder
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public abstract class AbstractReadHolder extends AbstractHolder implements ReadHolder {
    /**
     * Count the number of added heads when read sheet.
     *
     * <p>
     * 0 - This Sheet has no head ,since the first row are the data
     * <p>
     * 1 - This Sheet has one row head , this is the default
     * <p>
     * 2 - This Sheet has two row head ,since the third row is the data
     */
    private Integer headRowNumber;
    /**
     * Excel head property
     */
    private ExcelReadHeadProperty excelReadHeadProperty;
    /**
     * Read listener
     */
    private List<ReadListener<?>> readListenerList;

    public AbstractReadHolder(ReadBasicParameter readBasicParameter, excel.read.metadata.holder.AbstractReadHolder parentAbstractReadHolder) {
        super(readBasicParameter, parentAbstractReadHolder);

        if (readBasicParameter.getUseScientificFormat() == null) {
            if (parentAbstractReadHolder != null) {
                getGlobalConfiguration().setUseScientificFormat(
                    parentAbstractReadHolder.getGlobalConfiguration().getUseScientificFormat());
            }
        } else {
            getGlobalConfiguration().setUseScientificFormat(readBasicParameter.getUseScientificFormat());
        }

        // Initialization property
        this.excelReadHeadProperty = new ExcelReadHeadProperty(this, getClazz(), getHead());
        if (readBasicParameter.getHeadRowNumber() == null) {
            if (parentAbstractReadHolder == null) {
                if (excelReadHeadProperty.hasHead()) {
                    this.headRowNumber = excelReadHeadProperty.getHeadRowNumber();
                } else {
                    this.headRowNumber = 1;
                }
            } else {
                this.headRowNumber = parentAbstractReadHolder.getHeadRowNumber();
            }
        } else {
            this.headRowNumber = readBasicParameter.getHeadRowNumber();
        }

        if (parentAbstractReadHolder == null) {
            this.readListenerList = ListUtils.newArrayList();
        } else {
            this.readListenerList = ListUtils.newArrayList(parentAbstractReadHolder.getReadListenerList());
        }
        if (HolderEnum.WORKBOOK.equals(holderType())) {
            Boolean useDefaultListener = ((ReadWorkbook)readBasicParameter).getUseDefaultListener();
            if (useDefaultListener == null || useDefaultListener) {
                readListenerList.add(new ModelBuildEventListener());
            }
        }
        if (readBasicParameter.getCustomReadListenerList() != null
            && !readBasicParameter.getCustomReadListenerList().isEmpty()) {
            this.readListenerList.addAll(readBasicParameter.getCustomReadListenerList());
        }

        if (parentAbstractReadHolder == null) {
            setConverterMap(DefaultConverterLoader.loadDefaultReadConverter());
        } else {
            setConverterMap(new HashMap<>(parentAbstractReadHolder.getConverterMap()));
        }
        if (readBasicParameter.getCustomConverterList() != null
            && !readBasicParameter.getCustomConverterList().isEmpty()) {
            for (Converter<?> converter : readBasicParameter.getCustomConverterList()) {
                getConverterMap().put(
                    ConverterKeyBuild.buildKey(converter.supportJavaTypeKey(), converter.supportExcelTypeKey()),
                    converter);
            }
        }
    }

    @Override
    public List<ReadListener<?>> readListenerList() {
        return getReadListenerList();
    }

    @Override
    public ExcelReadHeadProperty excelReadHeadProperty() {
        return getExcelReadHeadProperty();
    }

}
