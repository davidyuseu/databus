package excel.metadata.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * formula
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class FormulaData {
    /**
     * formula
     */
    private String formulaValue;

    @Override
    public excel.metadata.data.FormulaData clone() {
        excel.metadata.data.FormulaData formulaData = new excel.metadata.data.FormulaData();
        formulaData.setFormulaValue(getFormulaValue());
        return formulaData;
    }
}
