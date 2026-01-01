package excel.read.metadata.property;

import excel.metadata.Holder;
import excel.metadata.property.ExcelHeadProperty;

import java.util.List;

/**
 * Define the header attribute of excel
 *
 * @author jipengfei
 */
public class ExcelReadHeadProperty extends ExcelHeadProperty {

    public ExcelReadHeadProperty(Holder holder, Class headClazz, List<List<String>> head) {
        super(holder, headClazz, head);
    }
}
