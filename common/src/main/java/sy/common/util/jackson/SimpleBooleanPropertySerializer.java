package sy.common.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;

public class SimpleBooleanPropertySerializer extends JsonSerializer<SimpleBooleanProperty> {
    public static final SimpleBooleanPropertySerializer INSTANCE = new SimpleBooleanPropertySerializer();

    @Override
    public void serialize(SimpleBooleanProperty simpleBooleanProperty, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeBoolean(simpleBooleanProperty.get());
    }
}
