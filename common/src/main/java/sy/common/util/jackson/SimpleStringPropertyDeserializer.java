package sy.common.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

public class SimpleStringPropertyDeserializer extends JsonDeserializer<SimpleStringProperty> {
    public static final SimpleStringPropertyDeserializer INSTANCE = new SimpleStringPropertyDeserializer();

    @Override
    public SimpleStringProperty deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String str = jsonParser.getText();
        return new SimpleStringProperty(str);
    }
}
