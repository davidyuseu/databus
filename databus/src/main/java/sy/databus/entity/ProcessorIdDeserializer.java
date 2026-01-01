package sy.databus.entity;

import sy.databus.organize.ProcessorManager;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class ProcessorIdDeserializer extends JsonDeserializer<ProcessorId> {
    public static final ProcessorIdDeserializer INSTANCE = new ProcessorIdDeserializer();
    @Override
    public ProcessorId deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        long pIdCode = jsonParser.getLongValue();
        return ProcessorManager.registerProcessorId(pIdCode);
    }
}
