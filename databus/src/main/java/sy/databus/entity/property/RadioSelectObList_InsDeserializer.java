package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonParser;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SJsonUtil;
import sy.databus.process.AbstractIntegratedProcessor;

import java.io.IOException;

@Log4j2
public class RadioSelectObList_InsDeserializer extends RadioSelectObListDeserializer<AbstractIntegratedProcessor> {

    @Override
    protected void getSelInfo(JsonParser jsonParser, RadioSelectObList<AbstractIntegratedProcessor> obList) throws IOException {
        if (obList instanceof RadioSelectObList_Ins obList_ins) {
            SJsonUtil.nextFieldValueToken(jsonParser, "selPId");
            long selPId = jsonParser.getLongValue();
            obList_ins.setSelPId(selPId);
        } else {
            log.error("{} only support to deserialize a {} obj!",
                    RadioSelectObList_InsDeserializer.class.getSimpleName(),
                    RadioSelectObList_Ins.class.getSimpleName());
        }
    }
}
