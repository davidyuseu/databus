package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AbstractMultiSelectObListSerializer extends JsonSerializer<AbstractMultiSelectObList> {

    public static final AbstractMultiSelectObListSerializer INSTANCE = new AbstractMultiSelectObListSerializer();

    @Override
    public void serialize(AbstractMultiSelectObList multiSelectObList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("selectedIndexes", multiSelectObList.getSelectedIndexes());
        jsonGenerator.writeEndObject();
    }
}
