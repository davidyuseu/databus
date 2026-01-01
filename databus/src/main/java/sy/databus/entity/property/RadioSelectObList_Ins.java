package sy.databus.entity.property;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import sy.databus.process.AbstractIntegratedProcessor;

@JsonDeserialize(using = RadioSelectObList_InsDeserializer.class)
@JsonSerialize(using = RadioSelectObList_InsSerializer.class)
public class RadioSelectObList_Ins extends RadioSelectObList<AbstractIntegratedProcessor> {
    @Getter @Setter
    private long selPId = -1L;

    public RadioSelectObList_Ins() {
        addIndexChangedAction((oldValue, newValue) -> {
            if (newValue >= 0)
                selPId = candidateList.get(newValue).getProcessorId().codec();
        });
    }

    public RadioSelectObList_Ins(boolean saveList, int selIndex) {
        super(saveList, selIndex);
    }

    @Override
    public String getItemString(AbstractIntegratedProcessor item) {
        return item.getNameValue();
    }
}
