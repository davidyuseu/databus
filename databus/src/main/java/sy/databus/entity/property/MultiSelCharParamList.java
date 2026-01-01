package sy.databus.entity.property;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import sy.common.tmresolve.charNum.CharParamProtocolStruct;
import sy.common.tmresolve.ResultStruct;

import java.util.LinkedHashSet;

//@JsonSerialize(using = AbstractMultiSelectObListSerializer.class)
public class MultiSelCharParamList extends AbstractMultiSelectObList<CharParamProtocolStruct, ResultStruct> {

    public MultiSelCharParamList(){}

    @Getter @Setter
    private int limitedFrameLen = 0; // 为0表示无参数帧长限制

    @Override
    public ResultStruct transToSelected(CharParamProtocolStruct selectingItem) {
        // translate the C into S
        ResultStruct resultStruct = new ResultStruct(selectingItem.getParamNum(), selectingItem.getParamName());
        resultStruct.setUnit(selectingItem.getUnit());
        return resultStruct;
    }

    @Override
    public boolean isHomologous(ResultStruct sel, CharParamProtocolStruct canItem) {
        return sel.getItemNum() == canItem.getParamNum();
    }

    @Override
    public String getItemString(CharParamProtocolStruct item) {
        // display the C by String on the ListView
        return item.getParamName();
    }

    public void limitedByFrameLen() {
        if (limitedFrameLen > 0) {
            for (int i = 0; i < candidateList.size(); i++) {
                var struct = candidateList.get(i);
                if (struct.getBytePos() >= limitedFrameLen) {
                    if (ignoredIndexes == null)
                        ignoredIndexes = new LinkedHashSet<>();
                    ignoredIndexes.add(i);
                    removeSelItem(i);
                }
            }
        } else {
            if (ignoredIndexes != null)
                ignoredIndexes.clear();
        }
    }

}
