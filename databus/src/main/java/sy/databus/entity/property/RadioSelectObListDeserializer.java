package sy.databus.entity.property;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.SneakyThrows;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.util.SJsonUtil;
import sy.databus.global.SSyncObservableListDeserializer;

import java.io.IOException;
import java.util.Objects;


public class RadioSelectObListDeserializer<T> extends JsonDeserializer<RadioSelectObList<T>> {

    public static final RadioSelectObListDeserializer INSTANCE = new RadioSelectObListDeserializer();

    @SneakyThrows
    @Override
    public RadioSelectObList<T> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        SJsonUtil.nextFieldValueToken(jsonParser, "className");
        String className = jsonParser.getValueAsString();
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Objects.requireNonNull(clazz, "The clazz to parse is null!");
        RadioSelectObList<T> obList = (RadioSelectObList<T>) clazz.getDeclaredConstructor().newInstance();
        getSelInfo(jsonParser, obList);
        SJsonUtil.nextFieldValueToken(jsonParser, "saveCandidateList");
        boolean saveCandidateList = jsonParser.getBooleanValue();
        obList.setSaveCandidateList(saveCandidateList);
        if (saveCandidateList) {
            SJsonUtil.nextFieldValueToken(jsonParser, "candidateList");
            SSyncObservableList candidateList = SSyncObservableListDeserializer.INSTANCE.deserialize(jsonParser, deserializationContext);
            // TODO
            SJsonUtil.nextObjectEnd(jsonParser);
            obList.setCandidateList(candidateList);
        }
        SJsonUtil.nextObjectEnd(jsonParser);
        return obList;
    }

    protected void getSelInfo(JsonParser jsonParser, RadioSelectObList<T> obList) throws IOException {
        SJsonUtil.nextFieldValueToken(jsonParser, "selIndex");
        int selIndex = jsonParser.getIntValue();
        obList.setSelIndex(selIndex);
    }
}
