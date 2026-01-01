package sy.common.util.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.StringUtil;

import java.io.IOException;

public class ByteBufSerializer extends JsonSerializer<ByteBuf> {
    public static final ByteBufSerializer INSTANCE = new ByteBufSerializer();

    @Override
    public void serialize(ByteBuf byteBuf, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (byteBuf != null) {
            byte[] bytes = new byte[byteBuf.writerIndex()];
            byteBuf.getBytes(0, bytes);
            jsonGenerator.writeString(
                    StringUtil.toHexStringPadded(bytes)
            );
        } else {
            jsonGenerator.writeNull();
        }
    }
}
