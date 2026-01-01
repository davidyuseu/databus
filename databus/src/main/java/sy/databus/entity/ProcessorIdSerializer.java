package sy.databus.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ProcessorIdSerializer extends JsonSerializer<ProcessorId> {
    public static final ProcessorIdSerializer INSTANCE = new ProcessorIdSerializer();
    @Override
    public void serialize(ProcessorId processorId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeNumber(processorId.getProcessorCode());
    }
}
