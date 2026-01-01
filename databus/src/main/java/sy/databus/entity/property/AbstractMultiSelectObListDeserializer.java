package sy.databus.entity.property;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class AbstractMultiSelectObListDeserializer extends JsonDeserializer<AbstractMultiSelectObList> {

    public static final AbstractMultiSelectObListDeserializer INSTANCE = new AbstractMultiSelectObListDeserializer();

    @Override
    public AbstractMultiSelectObList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }
}
