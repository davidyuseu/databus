package sy.databus.global;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.common.util.SJsonUtil;

import sy.databus.entity.ProcessorId;
import sy.databus.entity.ProcessorIdDeserializer;
import sy.databus.entity.ProcessorIdSerializer;
import sy.databus.entity.property.*;
import sy.databus.process.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;


public class ProcessorSJsonUtil extends SJsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorSJsonUtil.class);
    /**
     * 初始化ProcessorSJsonUtil，设置objectMapper的一些参数，
     * 该内容无法放到static块中进行，
     * 因为只会执行父类SJsonUtil中的static块一次，而不会再执行ProcessorSJsonUtil中的static块
     * 所以需要显式的调用
     * */
    public static void init() {

        /**
         * {@link ProcessorId}
         * */
        simpleModule.addSerializer(ProcessorId.class, ProcessorIdSerializer.INSTANCE);
        simpleModule.addDeserializer(ProcessorId.class, ProcessorIdDeserializer.INSTANCE);
        /**
         * {@link ConsecutiveBufSafeProperty}
         * */
        simpleModule.addSerializer(ConsecutiveBufSafeProperty.class, ConsecutiveBufSafePropertySerializer.INSTANCE);
        simpleModule.addDeserializer(ConsecutiveBufSafeProperty.class, ConsecutiveBufSafePropertyDeserializer.INSTANCE);
        /**
         * {@link AbstractHandler}
         * */
        simpleModule.addSerializer(AbstractHandler.class, AbstractHandlerSerializer.INSTANCE);
        simpleModule.addDeserializer(AbstractHandler.class, AbstractHandlerDeserializer.INSTANCE);
        /**
         * {@link AbstractIntegratedProcessor}
         * */
        simpleModule.addSerializer(AbstractIntegratedProcessor.class, AbstractIntegratedProcessorSerializer.INSTANCE);
        simpleModule.addDeserializer(AbstractIntegratedProcessor.class, AbstractIntegratedProcessorDeserializer.INSTANCE);
        /**
         * {@link SSyncObservableList}
         * */
        simpleModule.addSerializer(SSyncObservableList.class, SSyncObservableListSerializer.INSTANCE);
        simpleModule.addDeserializer(SSyncObservableList.class, SSyncObservableListDeserializer.INSTANCE);

        /**
         * {@link SFile}
         * */
        simpleModule.addSerializer(SFile.class, SFileSerializer.INSTANCE);
        simpleModule.addDeserializer(SFile.class, SFileDeserializer.INSTANCE);

        /**
         * {@link RadioSelectObList}
         * */
        simpleModule.addSerializer(RadioSelectObList.class, RadioSelectObListSerializer.INSTANCE);
        simpleModule.addDeserializer(RadioSelectObList.class, RadioSelectObListDeserializer.INSTANCE);

        /**
         * {@link AbstractMultiSelectObList}
         * */
//        simpleModule.addSerializer(AbstractMultiSelectObList.class, AbstractMultiSelectObListSerializer.INSTANCE);
//        objectMapper.registerModule(simpleModule);
    }

    /**
     * 序列化processor和handler中的各属性
     * {@link AbstractIntegratedProcessor}
     * {@link AbstractHandler}
     * */
    public static void processorObjSerializerHelper(Object processorObj, JsonGenerator jsonGenerator
            , SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("className", processorObj.getClass().getName());
        Class<?> clazz = processorObj.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Console.class)
                        && !field.getAnnotation(Console.class).config().equals(Console.Config.NONE)) {
                    field.setAccessible(true);
                    try {
                        if (Object.class.isAssignableFrom(field.getType())) {
                            if (field.getType() == BigInteger.class) {
                                jsonGenerator.writeNumberField(field.getName(), (BigInteger) field.get(processorObj));
                            } else if (field.getType() == BigDecimal.class) {
                                jsonGenerator.writeNumberField(field.getName(), (BigDecimal) field.get(processorObj));
                            } else {
                                jsonGenerator.writeObjectField(field.getName(), field.get(processorObj));
                            }
                        } else {
                            Type fieldType = field.getGenericType();
                            if (int.class.equals(fieldType)) {
                                jsonGenerator.writeNumberField(field.getName(), (Integer) field.get(processorObj));
                            } else if (short.class.equals(fieldType)) { // Number
                                jsonGenerator.writeNumberField(field.getName(), (Short) field.get(processorObj));
                            } else if (long.class.equals(fieldType)) {
                                jsonGenerator.writeNumberField(field.getName(), (Long) field.get(processorObj));
                            } else if (float.class.equals(fieldType)) {
                                jsonGenerator.writeNumberField(field.getName(), (Float) field.get(processorObj));
                            } else if (double.class.equals(fieldType)) {
                                jsonGenerator.writeNumberField(field.getName(), (Double) field.get(processorObj));
                            } else if (boolean.class.equals(fieldType)) {
                                jsonGenerator.writeBooleanField(field.getName(), (Boolean) field.get(processorObj));
                            } else if (byte.class.equals(fieldType)) {
                                jsonGenerator.writeNumberField(field.getName(), (Byte) field.get(processorObj));
                            } else {
                                LOGGER.error("{}", "Cannot serialize the unknown field type!");
                            }
                        }
                    } catch (IllegalAccessException e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
            if (clazz != AbstractIntegratedProcessor.class && clazz != AbstractHandler.class)
                clazz = clazz.getSuperclass();
            else
                break;
        }
        jsonGenerator.writeEndObject();
    }
}
