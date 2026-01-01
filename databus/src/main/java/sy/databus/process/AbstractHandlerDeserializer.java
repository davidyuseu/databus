package sy.databus.process;

import com.fasterxml.jackson.databind.JsonNode;
import sy.common.util.SJsonUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.SneakyThrows;

import java.io.IOException;

public class AbstractHandlerDeserializer extends JsonDeserializer<AbstractHandler> {
    public static final AbstractHandlerDeserializer INSTANCE = new AbstractHandlerDeserializer();

    @SneakyThrows
    @Override
    public AbstractHandler deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        SJsonUtil.nextFieldValueToken(jsonParser, "className");
        String className = jsonParser.getValueAsString();
        jsonParser.nextToken(); //移动游标，否则读取不了后续节点
        JsonNode treeNode = jsonParser.readValueAsTree();
        Class handlerClass = null;
        try {
            handlerClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (handlerClass != null) {
            AbstractHandler handler;
            if(treeNode != null)
                handler = (AbstractHandler) SJsonUtil.nodeToBean(treeNode, handlerClass);
            else
                handler = (AbstractHandler) handlerClass.getDeclaredConstructor().newInstance();
            handler.initialize();
            return handler;
        } else {
            throw new ProcessorInitException("Unknown class of the handler in deserializing!");
        }
    }
}
