package sy.databus.entity.property;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class SFileSerializer extends JsonSerializer<SFile> {

    public static final SFileSerializer INSTANCE = new SFileSerializer();

    @Override
    public void serialize(SFile sFile, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
            String resDirPath = SFile.RES_DIR.getCanonicalPath();
            String filePath = sFile.getCanonicalPath();
            if (filePath.contains(resDirPath)) {
                filePath = SFile.RES_DIR.getPath() + sFile.getCanonicalPath()
                        .substring(resDirPath.length());
                jsonGenerator.writeStringField("filePath", filePath);
            } else {
                jsonGenerator.writeStringField("filePath", sFile.getCanonicalPath());
            }
            jsonGenerator.writeStringField("description", sFile.getDescription());
            jsonGenerator.writeFieldName("extensions");
            jsonGenerator.writeArray(sFile.getExtensions(), 0, sFile.getExtensions().length);
        jsonGenerator.writeEndObject();
    }
}
