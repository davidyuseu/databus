package sy.databus.process;

import com.fasterxml.jackson.databind.JsonNode;
import sy.common.util.SJsonUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import sy.databus.process.frame.MessageSeriesProcessor;

import java.io.IOException;
import java.util.Objects;

@Log4j2
public class AbstractIntegratedProcessorDeserializer extends JsonDeserializer<AbstractIntegratedProcessor> {
    public static final AbstractIntegratedProcessorDeserializer INSTANCE = new AbstractIntegratedProcessorDeserializer();
    @SneakyThrows
    @Override
    public AbstractIntegratedProcessor deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        SJsonUtil.nextFieldValueToken(jsonParser, "className");
        String className = jsonParser.getValueAsString();
        jsonParser.nextToken(); //移动游标，否则读取不了后续节点
        JsonNode treeNode = jsonParser.readValueAsTree();
        Class processorClass = null;
        try {
            processorClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
        }
        if (processorClass != null) {
            AbstractIntegratedProcessor processor;
            if(treeNode != null && !treeNode.isEmpty()) {
                /** 该反序列化过程会调用Processor的无参构造器，如{@link MessageSeriesProcessor#MessageSeriesProcessor()}*/
                processor = (AbstractIntegratedProcessor) SJsonUtil.nodeToBean(treeNode, processorClass);
                if (Objects.requireNonNull(processor).getProcessorId() != null) {
                    processor.ensureProcessorId();
                } else log.warn("There is a null processorId in 'AbstractIntegratedProcessor' deserializing, ensure it is 'copy' operation!");
                processor.initialize();
                return processor;
            } else throw new ProcessorInitException("Cannot deserialize the 'AbstractIntegratedProcessor' from a none json string!");
        } else {
            throw new ProcessorInitException("Unknown class of the processor in deserializing!");
        }
    }
}
