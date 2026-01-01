package excel.write.metadata.fill;

import excel.enums.WriteTemplateAnalysisCellTypeEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Read the cells of the template while populating the data.
 *
 * @author Jiaju Zhuang
 **/
@Getter
@Setter
@EqualsAndHashCode
public class AnalysisCell {
    private int columnIndex;
    private int rowIndex;
    private List<String> variableList;
    private List<String> prepareDataList;
    private Boolean onlyOneVariable;
    private WriteTemplateAnalysisCellTypeEnum cellType;
    private String prefix;
    private Boolean firstRow;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        excel.write.metadata.fill.AnalysisCell that = (excel.write.metadata.fill.AnalysisCell)o;
        if (columnIndex != that.columnIndex) {
            return false;
        }
        return rowIndex == that.rowIndex;
    }

    @Override
    public int hashCode() {
        int result = columnIndex;
        result = 31 * result + rowIndex;
        return result;
    }
}
