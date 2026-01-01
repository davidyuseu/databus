package sy.databus.global;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.util.SJsonUtil;

import java.io.IOException;

public class SSyncObservableListSerializer extends JsonSerializer<SSyncObservableList> {
    public static final SSyncObservableListSerializer INSTANCE = new SSyncObservableListSerializer();

    public static final String CLASS_NAME = "className";
    public static final String ELEMENTS = "elements";
    public static final String EMPTY_LIST = "emptyList";

    @Override
    public void serialize(SSyncObservableList sSyncObservableList, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if(sSyncObservableList == null || sSyncObservableList.isEmpty()) {
            jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("className", EMPTY_LIST);
            jsonGenerator.writeEndObject();
        } else {
            jsonGenerator.writeStartObject();
            Class<?> clazz = sSyncObservableList.get(0).getClass();
            while (clazz != null && clazz != Object.class) {
                if (!SJsonUtil.isDeserializedTarget(clazz)) {
                    clazz = clazz.getSuperclass();
                    continue;
                }
                boolean flag = true;
                for (int i = 1; i < sSyncObservableList.size(); i++) {
                    if (!clazz.isAssignableFrom(sSyncObservableList.get(i).getClass())) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    break;
                } else {
                    clazz = clazz.getSuperclass();
                }
            }
            jsonGenerator.writeStringField("className", clazz.getName());
            jsonGenerator.writeObjectField("elements", sSyncObservableList.toArray());
            jsonGenerator.writeEndObject();
        }
    }
}
