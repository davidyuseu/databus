package sy.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.netty.buffer.ByteBuf;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
//import sy.common.tmresolve.ResultStruct;
import sy.common.util.jackson.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class SJsonUtil {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static final XmlMapper xmlMapper = new XmlMapper();

    private static List<Class<?>> serializedTargets = new ArrayList<>();
    private static List<Class<?>> deserializedTargets = new ArrayList<>();
    protected static SimpleModule simpleModule = new SimpleModule() {

        @Override
        public <T> SimpleModule addSerializer(Class<? extends T> type, JsonSerializer<T> ser) {
            SimpleModule simpleModule = super.addSerializer(type, ser);
            serializedTargets.add(type);
            return simpleModule;
        }

        @Override
        public <T> SimpleModule addDeserializer(Class<T> type, JsonDeserializer<? extends T> deser) {
            SimpleModule simpleModule = super.addDeserializer(type, deser);
            deserializedTargets.add(type);
            return simpleModule;
        }
    };

    public static boolean isSerializedTarget(Class target) {
        return serializedTargets.contains(target);
    }

    public static boolean isDeserializedTarget(Class target) {
        return deserializedTargets.contains(target);
    }

    protected static SimpleModule xmlSimpleModule = new SimpleModule();

    static {
        //忽略字段不匹配错误
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 设置转换模式
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
//        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 反序列化时全局忽略未知属性
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /**
         * {@link javafx.beans.property.SimpleBooleanProperty}
         * */
        simpleModule.addSerializer(SimpleBooleanProperty.class, SimpleBooleanPropertySerializer.INSTANCE);
        simpleModule.addDeserializer(SimpleBooleanProperty.class, SimpleBooleanPropertyDeserializer.INSTANCE);

        /**
         * {@link javafx.beans.property.SimpleStringProperty}
         * */
        simpleModule.addSerializer(SimpleStringProperty.class, SimpleStringPropertySerializer.INSTANCE);
        simpleModule.addDeserializer(SimpleStringProperty.class, SimpleStringPropertyDeserializer.INSTANCE);

        /**
         * {@link io.netty.buffer.ByteBuf}
         * */
        simpleModule.addSerializer(ByteBuf.class, ByteBufSerializer.INSTANCE);
        simpleModule.addDeserializer(ByteBuf.class, ByteBufDeserializer.INSTANCE);

        /**
         * {@link ResultStruct}
         * */
//        simpleModule.addSerializer(ResultStruct.class, ResultStructSerializer.INSTANCE);
//        simpleModule.addDeserializer(ResultStruct.class, ResultStructDeserializer.INSTANCE);

        objectMapper.registerModule(simpleModule);
        xmlMapper.registerModule(xmlSimpleModule);

    }

    /**
     * 创建 ObjectNode
     * @return
     */
    public static ObjectNode createJson() {
        return objectMapper.createObjectNode();
    }

    /**
     * 字符串转 java bean
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T strToBean(@NonNull String json, Class<T> clazz){
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static <T> T nodeToBean(@NonNull JsonNode node, Class<T> clazz) {
        return objectMapper.convertValue(node, clazz);
    }

    public static <T> T jsonFileToBean(@NonNull File file, Class<T> clazz) {
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * jsonNode 转 JsonNode
     * @param jsonNode
     * @return
     */
    public static <T> T jsonToBean(@NonNull JsonNode jsonNode, Class<T> clazz) {
        String json = jsonNode.toString();
        return strToBean(json, clazz);
    }

    /**
     * 字符串转容器等引用
     * @param json
     * @return
     */
    public static <T> T strToReference(@NonNull String json, Class<T> cls){
        try {
            return objectMapper.readValue(json, new TypeReference<T>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static <T> List<T> strToList(@NonNull String json, Class<T> cls) {
        try {
            return objectMapper.readValue(json, getCollectionType(List.class, cls));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static <T> List<T> nodeToList(@NonNull JsonNode node, Class<T> cls) {
        return objectMapper.convertValue(node, getCollectionType(List.class, cls));
    }

    public static <K, V> Map<K, V> strToMap(@NonNull String json, Class<K> keyClazz, Class<V> valueClazz) {
        try {
            return objectMapper.readValue(json, getCollectionType(Map.class, keyClazz, valueClazz));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private static JavaType getCollectionType(Class<?> collectionClazz, Class<?>... elementClasses ) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClazz, elementClasses);
    }

    /**
     * 字符串转 JsonNode
     * @param json
     * @return
     */
    public static JsonNode readTree(@NonNull String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * java bean 或者 Map 或者 JsonNode 转字符串
     * @param o
     * @return
     */
    public static String objToStr(@NonNull Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static void objToJsonFile(@NonNull Object o, @NonNull File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            objectMapper.writeValue(file, o);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * java bean 或者 Map 或者 JsonNode 转 JsonNode
     * @param o
     * @return
     */
    public static JsonNode objToJson(Object o) {
        JsonNode jsonNode = null;
        try {
            String jsonString = objectMapper.writeValueAsString(o);
            jsonNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return jsonNode;
    }

    public static String objToXMLStr(Object value) {
        try {
            String xml = xmlMapper.writeValueAsString(value);
            return xml;
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static void objToXMLFile(File resultFile, Object value) {
        try {
            xmlMapper.writeValue(resultFile, value);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public static JsonToken nextFieldValueToken(JsonParser jsonParser, String fieldName) throws IOException {
        while (!jsonParser.isClosed()) {
            JsonToken jsonToken = jsonParser.nextToken();
            if (JsonToken.FIELD_NAME.equals(jsonToken)
                    && jsonParser.getCurrentName().equals(fieldName)) {
                return jsonParser.nextToken();
            }
        }
        return jsonParser.currentToken();
    }

    public static void nextObjectEnd(JsonParser jsonParser) throws IOException {
        while (!JsonToken.END_OBJECT.equals(jsonParser.currentToken())) {
            jsonParser.nextToken();
        }
    }

}
