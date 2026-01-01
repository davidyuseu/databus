package sy.databus.organize;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.signal.ISignal;
import sy.databus.global.GlobalState;
import sy.databus.global.ProcessorType;
import sy.databus.process.AbstractIntegratedProcessor;

import sy.databus.process.Processor;
import sy.databus.process.ProcessorInitException;


import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
public class ProcessorManager {

    public static ObservableList<Class<? extends AbstractIntegratedProcessor>> gReplayFileReaders
            = FXCollections.observableArrayList();

    public static AbstractIntegratedProcessor gFileReader; // 全局的回放文件读取器

    @Getter
    private static Map<Long, ProcessorId> allProcessorIds = new HashMap<>();

    public static List<AbstractIntegratedProcessor> getAllProcessor() {
        return allProcessorIds.entrySet().stream()
                .map(entry -> entry.getValue().getOwner())
                .collect(Collectors.toList());
    }

    @Getter
    private static Map<Long, ProcessorId> authorizedProcessorIds = new HashMap<>();

    // 起点处理器
    @Getter
    private static final List<AbstractIntegratedProcessor> sourceProcessors = new LinkedList<>();
    public static boolean addSource(AbstractIntegratedProcessor source) {
        return sourceProcessors.add(source);
    }

    public static void clearSources() {
        sourceProcessors.clear();
    }

    /**
     * 判断是否已录入ProcessorId，但注意，该ProcessorId未必有AbstractIntegratedProcessor实体（owner）
     * */
    public static boolean containsPId(long processorCode) {
        return allProcessorIds.containsKey(processorCode);
    }

    public static boolean containsAuthorizedPId(long processorCode) {return authorizedProcessorIds.containsKey(processorCode);}

    public static ProcessorId getPId(long processorCode) {
        return allProcessorIds.get(processorCode);
    }

    public static ProcessorId getAuthorizedPId(long processorCode) {
        ProcessorId pId = authorizedProcessorIds.get(processorCode);
        if (pId == null)
            log.error("PId: {} was not authorized!", processorCode);
        return pId;
    }

    /**
     * 判断是否已录入ProcessorId，且该ProcessorId有AbstractIntegratedProcessor实体（owner）
     * */
    public static boolean isRegister(long processorCode) {
        ProcessorId pId = allProcessorIds.get(processorCode);
        return pId != null && pId.getOwner() != null;
    }

    public static AbstractIntegratedProcessor getProcessor(long processorCode) {
        ProcessorId pId = allProcessorIds.get(processorCode);
        return pId != null ? pId.getOwner() : null;
    }

    /**
     * 根据AbstractIntegratedProcessor各子类的注解{@link Processor#type()}来确定处理器类型，
     * 并分配唯一的ProcessorId，其中的procNum是自增的
     * */
    public static ProcessorId allocateProcessorId(short seatNum,
                                                  Class<? extends AbstractIntegratedProcessor> processorClazz
        ) {
        if(allProcessorIds.size() > Short.MAX_VALUE)
            throw new ProcessorInitException("无可分配的处理器Id！");
        Processor processorAnno = processorClazz.getAnnotation(Processor.class);
        ProcessorType processorType = processorAnno.type();
        short procNum = 0;
        return registerProcessorId(seatNum, processorType, procNum);
    }

    /**
     * 以输入的处理器类(processorClazz)和默认的席位号(Initializer.seatNum)分配一个ProcessorId
     * */
    public static ProcessorId allocateProcessorId(Class<? extends AbstractIntegratedProcessor> processorClazz) {
        return allocateProcessorId(GlobalState.seatNum, processorClazz);
    }

    public static void allocateProcessorId(AbstractIntegratedProcessor processor) {
        ProcessorId pId = allocateProcessorId(GlobalState.seatNum, processor.getClass());
        processor.bindProcessorId(pId);
    }

    /**
     * 以输入的席位号(seatNum)、处理器类型(processorType)、处理器号(procNum)注册一个ProcessorId
     * */
    public static ProcessorId registerProcessorId(short seatNum, ProcessorType processorType, short procNum) {
        ProcessorId processorId = null;
        while(procNum < Short.MAX_VALUE){
            long processorCode = ProcessorId.codec(seatNum, processorType, procNum);
            if(!allProcessorIds.containsKey(processorCode)) {
                processorId = new ProcessorId(seatNum, processorType, procNum, processorCode);
                putNewPId(processorCode, processorId);
                break;
            }
            procNum++;
        }
        if(processorId == null)
            throw new ProcessorInitException("Fail to allocate a ProcessorId!");

        return processorId;
    }

    /**
     * 用于对处理器配置文件反序列化时生成ProcessorId，{@link sy.databus.entity.ProcessorIdDeserializer}
     * */
    public static ProcessorId registerProcessorId(long processorCode) {
        if(allProcessorIds.size() > Short.MAX_VALUE)
            throw new ProcessorInitException("无可分配的处理器Id！");
        ProcessorId processorId = ProcessorId.decode(processorCode);
        if(!allProcessorIds.containsKey(processorCode)) {
            putNewPId(processorCode, processorId);
            return processorId;
        }else {
            return null;
        }
    }

    private static void putNewPId(long processorCode, ProcessorId id) {
        allProcessorIds.put(processorCode, id);
    }

    public static void removeProcessorId(long processorCode) {
        allProcessorIds.get(processorCode).setOwner(null);
        if(allProcessorIds.containsKey(processorCode))
            allProcessorIds.remove(processorCode);
    }

    public static void removeProcessorId(ProcessorId processorId) {
        removeProcessorId(processorId.getProcessorCode());
    }

    public static void clearAllPId() {
        allProcessorIds.clear();
    }

    /* sig and slot manager*/
    /**
     * 添加信号槽
     * @param append 是否将槽追加至槽列表末
     * */
    public static void addSlot(Map<Class<? extends ISignal>, List<Function<ISignal, Boolean>>> slots,
                               Class<? extends ISignal> sig,
                               boolean append,
                               Function<ISignal, Boolean> slot) {
        var list = slots.computeIfAbsent(sig, k -> new ArrayList<>());
        list.add(append ? list.size() : 0, slot);
    }

}
