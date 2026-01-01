package sy.databus.entity.property;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import sy.common.util.SJsonUtil;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SFileDeserializer extends JsonDeserializer<SFile> {

    public static final SFileDeserializer INSTANCE = new SFileDeserializer();

    @Override
    public SFile deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        SJsonUtil.nextFieldValueToken(jsonParser, "filePath");
        File tFile = new File(jsonParser.getValueAsString());

        if (Objects.requireNonNull(tFile).isDirectory()) {
            return SFile.buildDirectory(tFile, false, false);
        } else {
            SJsonUtil.nextFieldValueToken(jsonParser, "description");
            String description = jsonParser.getValueAsString();
            SJsonUtil.nextFieldValueToken(jsonParser, "extensions");
            List<String> listExtensions = new ArrayList<>();
            while (!jsonParser.currentToken().equals(JsonToken.END_ARRAY)) {
                if (jsonParser.currentToken().equals(JsonToken.VALUE_STRING))
                    listExtensions.add(jsonParser.getValueAsString());
                jsonParser.nextToken();
            }
            SJsonUtil.nextObjectEnd(jsonParser);
            String[] arrayExtensions = Objects.requireNonNull(listExtensions)
                    .toArray(new String[listExtensions.size()]);
            return SFile.buildFile(tFile, description, false, false, arrayExtensions);
        }
    }
}
