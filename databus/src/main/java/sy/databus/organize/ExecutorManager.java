package sy.databus.organize;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.ProcessorId;
import sy.databus.global.ProcessorType;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.AbstractMessageProcessor;
import sy.databus.process.Processor;
import sy.databus.process.ProcessorInitException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Log4j2
public class ExecutorManager {
    /** -> EventLoopGroup ---------------------------------------------------------------------------------------------*/
      /**-> State --------------------------------------------------------*/
    public static final int READY_EVENT_LOOP_GROUP = 1;
    public static final int SHUTDOWN_EVENT_LOOP_GROUP = 2;
    private static final Object eGroupMutex = new Object();
      /**<- State ---------------------------------------------------------*/
    @Getter
    private static volatile int state_EventLoopGroup = SHUTDOWN_EVENT_LOOP_GROUP;
    public static final int CPU_CORES_NUM = Runtime.getRuntime().availableProcessors();
    private static NioEventLoopGroup group;

    public static void shutdownEventLoopGroup() {
        synchronized (eGroupMutex) {
            try {
                log.info("Shutting down the 'EventLoopGroup', please wait a shot!");
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            state_EventLoopGroup = SHUTDOWN_EVENT_LOOP_GROUP;
        }
    }

    public static EventLoopGroup allocateEventLoopGroup() {
        synchronized (eGroupMutex) {
            if (state_EventLoopGroup != READY_EVENT_LOOP_GROUP) {
                log.info("'EventLoopGroup' being created!");
                return createEventLoopGroup(0); // TODO 后期考虑从初始化配置文件中读取tCount
            }
            return group;
        }
    }

    private static EventLoopGroup createEventLoopGroup(int tCount) {
        synchronized (eGroupMutex) {
            if (tCount != 0)
                group = new NioEventLoopGroup(tCount);
            else
                group = new NioEventLoopGroup(CPU_CORES_NUM);

            state_EventLoopGroup = READY_EVENT_LOOP_GROUP;
            return group;
        }
    }
    /** <- EventLoopGroup ---------------------------------------------------------------------------------------------*/

    /** -> ReplayFileReader -------------------------------------------------------------------------------------------*/
    private static ExecutorService allocateSingleExecutor() {
        return Executors.newSingleThreadExecutor();
    }
    /** <- ReplayFileReader -------------------------------------------------------------------------------------------*/

    /** -> 帧处理线程池------------------------------------------------------------------------------------------------*/
    private static Map<ExecutorService, ArrayList<ProcessorId>> mpscMsgExecutors = new HashMap<>();
    /** -> 耗时IO线程池------------------------------------------------------------------------------------------------*/
    private static Map<ExecutorService, ArrayList<ProcessorId>> mpscIOExecutors = new HashMap<>();

    private static ExecutorService allocateMpscExecutor(@NonNull ProcessorId processorId,
                                                        Map<ExecutorService, ArrayList<ProcessorId>> mpscExecutors) {
        // TODO 轮询分配Executor，每次分配processor挂载数目最少的SingleThreadExecutor
        ExecutorService executor = null;
        if (mpscExecutors.size() < CPU_CORES_NUM ) {
            executor = Executors.newSingleThreadExecutor();
            mpscExecutors.put(executor, new ArrayList<>() {{add(processorId);}});
        } else {
            int count = -1;
            List pIds = null;
            for (Map.Entry<ExecutorService, ArrayList<ProcessorId>> entry : mpscExecutors.entrySet()) {
                if (count < 0 || entry.getValue().size() < count ) {
                    executor = entry.getKey();
                    count = entry.getValue().size();
                    pIds = entry.getValue();
                }
            }
            pIds.add(processorId);
        }
        return executor;
    }

    public static void allocateExecutor(AbstractIntegratedProcessor processor) {
        if (!processor.isAsynchronous()) {// 不需要执行器的不分配
            processor.setExecutor(null);
            return;
        }
        if (processor instanceof AbstractMessageProcessor msgProcessor && msgProcessor.getCopier() == null) {
            log.warn("The processor of {} is asynchronous but without a copier!",
                    processor.getClass().getSimpleName());
        }
        if (processor.getExecutor() != null
                && !processor.getExecutor().isShutdown()
                && !processor.getExecutor().isTerminated()) // 已分配的不再分配
            return;
        Processor processorAnno = processor.getClass().getAnnotation(Processor.class);
        ProcessorType.Category category = processorAnno.type().getCategory();
        ExecutorService executor;
        switch (category) {
            // 回放器具有独立的线程执行器，切换模式触发停止工作方法#resetSync，单独将其放进switchingPool去终结工作
            case PROCESSOR_SINGLE_EXECUTOR -> {executor = allocateSingleExecutor();}
            // 设备处理器在boot()时自行加入EventLoopGroup或创建独占执行器；在#resetSync方法中close掉，并进入TERMINATED状态
            /*case PROCESSOR_SOCKET, */
            case PROCESSOR_MPSC_COMPUTING -> {executor = allocateMpscExecutor(processor.getProcessorId(), mpscMsgExecutors);}
            case PROCESSOR_MPSC_IO -> {executor = allocateMpscExecutor(processor.getProcessorId(), mpscIOExecutors);}
            case PROCESSOR_NIO -> {executor = allocateEventLoopGroup();}
            default -> throw new ProcessorInitException("This is an unknown category of processor to allocate executor!");
        }
        processor.setExecutor(executor);
    }

    public static void deallocateExecutors() {
        deallocateExecutors(mpscMsgExecutors);
        deallocateExecutors(mpscIOExecutors);
        shutdownEventLoopGroup();
    }

    private static void deallocateExecutors(Map<ExecutorService, ArrayList<ProcessorId>> mpscExecutors) {
        for (var entry : mpscExecutors.entrySet()) {
            var pIds = entry.getValue();
            for (int i = 0; i < entry.getValue().size(); i++) {
                pIds.get(i).getOwner().setExecutor(null);
            }
            pIds.clear();
        }
    }

    public static ArrayList<ExecutorService> exclusiveExecutors = new ArrayList<>(CPU_CORES_NUM);
    /**
     * 创建独占执行器池
     * */
    static {
        for (int i = 0; i < CPU_CORES_NUM; i++) {
            exclusiveExecutors.add(Executors.newSingleThreadExecutor());
        }
    }

}
