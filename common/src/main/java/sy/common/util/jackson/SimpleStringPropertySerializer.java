package sy.common.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.beans.property.SimpleStringProperty;

import java.io.IOException;

public class SimpleStringPropertySerializer extends JsonSerializer<SimpleStringProperty> {
    public static final SimpleStringPropertySerializer INSTANCE = new SimpleStringPropertySerializer();

    @Override
    public void serialize(SimpleStringProperty simpleStringProperty, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(simpleStringProperty.get());
    }
}
