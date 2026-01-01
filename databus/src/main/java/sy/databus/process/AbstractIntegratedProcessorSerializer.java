package sy.databus.process;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import sy.databus.global.ProcessorSJsonUtil;

import java.io.IOException;

public class AbstractIntegratedProcessorSerializer extends JsonSerializer<AbstractIntegratedProcessor> {
    public static final AbstractIntegratedProcessorSerializer INSTANCE = new AbstractIntegratedProcessorSerializer();
    @Override
    public void serialize(AbstractIntegratedProcessor abstractIntegratedProcessor, JsonGenerator jsonGenerator
            , SerializerProvider serializerProvider) throws IOException {
        ProcessorSJsonUtil.processorObjSerializerHelper(abstractIntegratedProcessor, jsonGenerator, serializerProvider);
    }
}
