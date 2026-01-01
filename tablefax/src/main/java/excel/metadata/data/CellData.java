package excel.metadata.data;

import excel.enums.CellDataTypeEnum;
import excel.metadata.AbstractCell;
import excel.util.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Excel internal cell data.
 *
 * <p>
 *
 * @author Jiaju Zhuang
 */
@Getter
@Setter
@EqualsAndHashCode
public class CellData<T> extends AbstractCell {
    /**
     * cell type
     */
    private CellDataTypeEnum type;
    /**
     * {@link CellDataTypeEnum#NUMBER}
     */
    private BigDecimal numberValue;
    /**
     * {@link CellDataTypeEnum#STRING} and{@link CellDataTypeEnum#ERROR}
     */
    private String stringValue;
    /**
     * {@link CellDataTypeEnum#BOOLEAN}
     */
    private Boolean booleanValue;

    /**
     * The resulting converted data.
     */
    private T data;

    /**
     * formula
     */
    private FormulaData formulaData;

    /**
     * Ensure that the object does not appear null
     */
    public void checkEmpty() {
        if (type == null) {
            type = CellDataTypeEnum.EMPTY;
        }
        switch (type) {
            case STRING:
            case ERROR:
                if (StringUtils.isEmpty(stringValue)) {
                    type = CellDataTypeEnum.EMPTY;
                }
                return;
            case NUMBER:
                if (numberValue == null) {
                    type = CellDataTypeEnum.EMPTY;
                }
                return;
            case BOOLEAN:
                if (booleanValue == null) {
                    type = CellDataTypeEnum.EMPTY;
                }
                return;
            default:
        }
    }


}
