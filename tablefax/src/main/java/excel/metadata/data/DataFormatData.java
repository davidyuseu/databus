package excel.metadata.data;

import excel.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * data format
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class DataFormatData {
    /**
     * index
     */
    private Short index;

    /**
     * format
     */
    private String format;

    /**
     * The source is not empty merge the data to the target.
     *
     * @param source source
     * @param target target
     */
    public static void merge(excel.metadata.data.DataFormatData source, excel.metadata.data.DataFormatData target) {
        if (source == null || target == null) {
            return;
        }
        if (source.getIndex() != null) {
            target.setIndex(source.getIndex());
        }
        if (StringUtils.isNotBlank(source.getFormat())) {
            target.setFormat(source.getFormat());
        }
    }

    @Override
    public excel.metadata.data.DataFormatData clone() {
        excel.metadata.data.DataFormatData dataFormatData = new excel.metadata.data.DataFormatData();
        dataFormatData.setIndex(getIndex());
        dataFormatData.setFormat(getFormat());
        return dataFormatData;
    }
}
