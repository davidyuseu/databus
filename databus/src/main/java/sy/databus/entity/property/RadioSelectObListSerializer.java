package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class RadioSelectObListSerializer<T> extends JsonSerializer<RadioSelectObList<T>> {
    public static final RadioSelectObListSerializer INSTANCE = new RadioSelectObListSerializer();

    @Override
    public void serialize(RadioSelectObList<T> radioSelectObList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("className", radioSelectObList.getClass().getName());
            writeSelItem(radioSelectObList, jsonGenerator);
            boolean saveCandidateList = radioSelectObList.isSaveCandidateList();
            jsonGenerator.writeBooleanField("saveCandidateList", saveCandidateList);
            if (saveCandidateList) // 需要存储候选列表
                jsonGenerator.writeObjectField("candidateList", radioSelectObList.getCandidateList());
        jsonGenerator.writeEndObject();
    }

    protected void writeSelItem(RadioSelectObList<T> radioSelectObList, JsonGenerator jsonGenerator) throws IOException {
        jsonGenerator.writeNumberField("selIndex", radioSelectObList.getSelIndex());
    }
}
