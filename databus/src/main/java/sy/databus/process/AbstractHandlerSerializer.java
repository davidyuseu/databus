package sy.databus.process;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import sy.databus.global.ProcessorSJsonUtil;

import java.io.IOException;

public class AbstractHandlerSerializer extends JsonSerializer<AbstractHandler> {
    public static final AbstractHandlerSerializer INSTANCE = new AbstractHandlerSerializer();
    @Override
    public void serialize(AbstractHandler abstractFrameHandler, JsonGenerator jsonGenerator
            , SerializerProvider serializerProvider) throws IOException {
        ProcessorSJsonUtil.processorObjSerializerHelper(abstractFrameHandler, jsonGenerator, serializerProvider);
    }
}
