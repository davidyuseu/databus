package sy.databus.entity.property;


import lombok.Getter;
import lombok.Setter;
import sy.common.tmresolve.ResultStruct;
import sy.common.tmresolve.gen.vector.ParamTableItem;

import java.util.LinkedHashSet;

//@JsonSerialize(using = AbstractMultiSelectObListSerializer.class)
public class MultiSelGenParamList extends AbstractMultiSelectObList<ParamTableItem, ResultStruct>{

    public MultiSelGenParamList() {}

    @Getter @Setter
    private int limitedFrameLen = 0; // 为0表示无参数帧长限制

    @Override
    public ResultStruct transToSelected(ParamTableItem selectingItem) {
        // translate the C into S
        ResultStruct resultStruct = new ResultStruct(selectingItem.getParamNum(), selectingItem.getParamName());
        resultStruct.setUnit(selectingItem.getParamUnit());
        return resultStruct;
    }

    @Override
    public boolean isHomologous(ResultStruct sel, ParamTableItem canItem) {
        return sel.getItemNum() == canItem.getParamNum();
    }

    @Override
    public String getItemString(ParamTableItem item) {
        // display the C by String on the ListView
        return item.getParamName();
    }

    public void limitedByFrameLen() {
        if (limitedFrameLen > 0) {
            for (int i = 0; i < candidateList.size(); i++) {
                var struct = candidateList.get(i);
                if (struct.getBytePos() + struct.getBytesLen() > limitedFrameLen) {
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
