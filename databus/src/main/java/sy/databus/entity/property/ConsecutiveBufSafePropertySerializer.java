package sy.databus.entity.property;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.netty.util.internal.StringUtil;

import java.io.IOException;

public class ConsecutiveBufSafePropertySerializer extends JsonSerializer<ConsecutiveBufSafeProperty> {
    public static final ConsecutiveBufSafePropertySerializer INSTANCE = new ConsecutiveBufSafePropertySerializer();
    @Override
    public void serialize(ConsecutiveBufSafeProperty consecutiveBufSafeProperty, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(
                StringUtil.toHexStringPadded(
                    consecutiveBufSafeProperty.getValue().array()
                )
        );
    }
}
