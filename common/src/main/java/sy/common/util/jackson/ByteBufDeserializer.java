package sy.common.util.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteUtil;

import java.io.IOException;

@Log4j2
public class ByteBufDeserializer extends JsonDeserializer<ByteBuf> {
    public static final ByteBufDeserializer INSTANCE = new ByteBufDeserializer();

    @SneakyThrows
    @Override
    public ByteBuf deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        String input = jsonParser.getText();
        if (input.isEmpty())
            return null;
        return SByteUtil.getBufByString(input);
    }
}
