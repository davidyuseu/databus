package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonGenerator;
import lombok.extern.log4j.Log4j2;
import sy.databus.process.AbstractIntegratedProcessor;

import java.io.IOException;

@Log4j2
public class RadioSelectObList_InsSerializer extends RadioSelectObListSerializer<AbstractIntegratedProcessor> {
    @Override
    protected void writeSelItem(RadioSelectObList<AbstractIntegratedProcessor> radioSelectObList, JsonGenerator jsonGenerator) throws IOException {
        if (radioSelectObList instanceof RadioSelectObList_Ins radioSelectObList_ins) {
            jsonGenerator.writeNumberField("selPId", radioSelectObList_ins.getSelPId());
        } else {
            log.error("{} only support to serialize a {} obj!",
                    RadioSelectObList_InsSerializer.class.getSimpleName(),
                    RadioSelectObList_Ins.class.getSimpleName());
        }
    }
}
