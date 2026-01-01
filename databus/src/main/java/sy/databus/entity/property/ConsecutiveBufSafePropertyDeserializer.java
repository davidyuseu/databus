package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ConsecutiveBufSafePropertyDeserializer extends JsonDeserializer<ConsecutiveBufSafeProperty> {
    public static final ConsecutiveBufSafePropertyDeserializer INSTANCE = new ConsecutiveBufSafePropertyDeserializer();
    @Override
    public ConsecutiveBufSafeProperty deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String input = jsonParser.getText();
        return new ConsecutiveBufSafeProperty(input);
    }

}
