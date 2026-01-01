package sy.common.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;

public class SimpleBooleanPropertyDeserializer extends JsonDeserializer<SimpleBooleanProperty> {
    public static final SimpleBooleanPropertyDeserializer INSTANCE = new SimpleBooleanPropertyDeserializer();

    @Override
    public SimpleBooleanProperty deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        boolean value = jsonParser.getBooleanValue();
        return new SimpleBooleanProperty(value);
    }
}
