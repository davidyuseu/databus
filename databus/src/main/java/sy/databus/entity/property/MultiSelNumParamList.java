package sy.databus.entity.property;

import lombok.Getter;
import lombok.Setter;
import sy.common.tmresolve.charNum.NumParamProtocolStruct;
import sy.common.tmresolve.ResultStruct;
import sy.databus.view.controller.MultiSelNumParamListController;

import java.util.LinkedHashSet;

/**
 *  Controller: {@link MultiSelNumParamListController}
 * */
//@JsonSerialize(using = AbstractMultiSelectObListSerializer.class)
public class MultiSelNumParamList extends AbstractMultiSelectObList<NumParamProtocolStruct, ResultStruct> {

    public MultiSelNumParamList() {}

    @Setter @Getter
    private int limitedFrameLen = 0; // 为0表示无参数帧长限制

    @Override
    public ResultStruct transToSelected(NumParamProtocolStruct selectingItem) {
        // translate the C into S
        ResultStruct resultStruct = new ResultStruct(selectingItem.getParamNum(), selectingItem.getParamName());
        resultStruct.setUnit(selectingItem.getUnit());
        return resultStruct;
    }

    @Override
    public boolean isHomologous(ResultStruct sel, NumParamProtocolStruct canItem) {
        return sel.getItemNum() == canItem.getParamNum();
    }

    @Override
    public String getItemString(NumParamProtocolStruct item) {
        // display the C by String on the ListView
        return item.getParamName();
    }

    public void limitedByFrameLen() {
        if (limitedFrameLen > 0) {
            for (int i = 0; i < candidateList.size(); i++) {
                var struct = candidateList.get(i);
                if (struct.getBytePos() + struct.getByteCount() > limitedFrameLen) {
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
