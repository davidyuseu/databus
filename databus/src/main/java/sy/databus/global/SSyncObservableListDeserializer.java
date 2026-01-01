package sy.databus.global;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.util.SJsonUtil;
import sy.databus.process.AbstractHandler;
import sy.databus.process.ProcessorInitException;

import java.io.IOException;
import java.util.List;

import static sy.databus.global.SSyncObservableListSerializer.EMPTY_LIST;

@Log4j2
public class SSyncObservableListDeserializer extends JsonDeserializer<SSyncObservableList> {
    public static final SSyncObservableListDeserializer INSTANCE = new SSyncObservableListDeserializer();
    @Override
    public SSyncObservableList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        SJsonUtil.nextFieldValueToken(jsonParser, SSyncObservableListSerializer.CLASS_NAME);
        String className = jsonParser.getValueAsString();
        if (className.equals(EMPTY_LIST)) {
            SJsonUtil.nextObjectEnd(jsonParser);
            return new SSyncObservableList();
        }
        SJsonUtil.nextFieldValueToken(jsonParser, SSyncObservableListSerializer.ELEMENTS);
        JsonNode treeNode = jsonParser.readValueAsTree();
        Class elementClass = null;
        try {
            elementClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        List list;
        if (elementClass != null) {
            list = SJsonUtil.nodeToList(treeNode, elementClass);
        } else {
            throw new ProcessorInitException("Unknown class of the SSyncObservableList in deserializing!");
        }
        SJsonUtil.nextObjectEnd(jsonParser);
        return new SSyncObservableList(FXCollections.observableArrayList(list));
    }
}
