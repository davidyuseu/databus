package sy.databus.view.controller;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.property.RadioSelectObList_Ins;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Console;

import java.lang.reflect.Field;

@Log4j2
public class RadioSelObList_InsController extends RadioSelObListController<AbstractIntegratedProcessor> {

    public RadioSelObList_InsController(Console console, Field field, Object parentObj, @NonNull RadioSelectObList radioSelObList) {
        super(console, field, parentObj, radioSelObList);
    }

    @Override
    protected void selectCrntItem() {
        if (radioSelectObList instanceof RadioSelectObList_Ins radioSelectObList_ins) {
            int index = -1;
            long selPId = radioSelectObList_ins.getSelPId();
            var candidateList = radioSelectObList_ins.getCandidateList();
            if (selPId != -1 && candidateList != null){
                for (int i = 0; i < candidateList.size(); i++) {
                    if (candidateList.get(i).getProcessorId().codec() == selPId) {
                        index = i;
                        break;
                    }
                }
            }
            comboBox.getSelectionModel().select(index);
        } else {
            log.error("{} is adapted to {}!", RadioSelObList_InsController.class.getSimpleName()
                    , RadioSelectObList_Ins.class.getSimpleName());
        }
    }
}
