package sy.databus.process;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.EvictingQueue;
import io.netty.util.internal.StringUtil;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.databus.entity.IEvent;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.property.IDeletionResponder;
import sy.databus.entity.property.SimpleStrProperty;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.entity.signal.DATA_TASK_END;
import sy.databus.entity.signal.ISignal;
import sy.databus.entity.signal.TASK_FINISH;
import sy.databus.global.WorkMode;
import sy.databus.organize.*;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.process.fsm.PopularState;
import sy.databus.process.fsm.SyncObservableState;
import sy.grapheditor.core.connections.Connectable;
import sy.grapheditor.model.GNode;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;

import static io.netty.util.internal.StringUtil.NEWLINE;
import static sy.databus.process.Console.Config.IMPLICIT;
import static sy.databus.process.Console.Config.STATIC;
import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;

@JsonIgnoreProperties(ignoreUnknown = true,
        value = {"procMutex","firstHandler","previousProcessors","nextProcessors","executor","crntState","infoReporter"})
@Log4j2
public abstract class AbstractIntegratedProcessor
        implements IEventProc<IEvent>, IRouter<IEvent>, Connectable, ComponentCustomization {

    // 综合处理器的同步锁，它和其中各handler的行为由procMutex同步
    @Getter
    protected final Object procMutex = new Object();

    @Getter
    @Console(config = IMPLICIT)
    protected ProcessorId processorId;
    @Getter
    protected long pIdValue;

    public void bindProcessorId(final ProcessorId pId) {
        if (pId != null) {
            this.processorId = pId;
            this.processorId.setOwner(this);
            this.pIdValue =  processorId.codec();
        } else
            log.warn("{}","this processor is binding a none processorId!");
    }
    public void ensureProcessorId() {
        if (processorId != null) {
            processorId.setOwner(this);
            this.pIdValue = processorId.codec();
        } else {
            log.warn("{}", "this processor is ensuring a none processorId!");
        }
    }

    @Getter @Setter
    @Console(config = IMPLICIT)
    // 对是否异步的操作仅能在“编辑模式”下进行，此时任何执行器都没有任务执行，可以直接回收
    protected volatile boolean asynchronous = true;

    @Getter @Setter
    @Console(config = STATIC, display = "监控")
    protected boolean toReportInfo = false;

    @Getter @Setter
    @Console(config = STATIC, display = "名称")
    protected SimpleStrProperty name = new SimpleStrProperty("IntegratedProcessor");
    public void setNameValue(String name) { this.name.set(name); }
    public String getNameValue() { return this.name.get(); }

    protected void initializeName(String name) {
        if (getNameValue().equals(getClass().getSimpleName()))
            setNameValue(name);
    }

    @Getter
    protected SyncObservableState crntState = new SyncObservableState();

    // 各综合处理器维护各自的Handlers，它们之间的消息传递构成数据传输的第一维度
    @Getter
    protected IEventProc firstHandler = this; // 首处理器指针

    protected SSyncObservableList<AbstractIntegratedProcessor> previousProcessors
            = new SSyncObservableList(procMutex); // 引用上一级所有的综合处理器
    @Getter
    protected SSyncObservableList<AbstractIntegratedProcessor> nextProcessors
            = new SSyncObservableList(procMutex); // 引用下一级所有的综合处理器

    @Getter @Setter
    protected ExecutorService executor;

    public void execute(Runnable task) throws Exception {
        executor.execute(task);
    }

    public Future<?> submit(Callable<?> task) {
        return executor.submit(task);
    }

    protected AbstractIntegratedProcessor() {
    }

    protected AbstractIntegratedProcessor(ProcessorId processorId) {
        this.processorId = processorId;
        this.processorId.setOwner(this);
    }

    // 各综合处理器之间可以连接成信号传递流，此为数据传输的第二维度
    protected IDataProc signalHandler;

    // 综合处理器可以响应全局事件（非数据流和信号流事件），此为数据传输的第三维度
    protected IDataProc globalEventHandler;

    /**
     * 由于反序列化时调用无参构造方法来实例化，此后可能会有部分成员变量被初始化为null
     * 所以框架需要调用initialize()方法再对它们进行初始化
     * */
    public void initialize() {

        if (previousProcessors == null)
            previousProcessors = new SSyncObservableList<>(procMutex);
        else
            previousProcessors.setMutex(procMutex);

        if (nextProcessors == null)
            nextProcessors = new SSyncObservableList<>(procMutex);
        else
            nextProcessors.setMutex(procMutex);

        previousProcessors.addListener((ListChangeListener<AbstractIntegratedProcessor>) change -> {
            while (change.next()) {
                sourceFlag = previousProcessors.size() <= 0;
            }
        });

        nextProcessors.addListener((ListChangeListener<AbstractIntegratedProcessor>) change -> {
            while (change.next()) {
                endFlag = nextProcessors.size() <= 0;
            }
        });

        appendSlot(DATA_TASK_BEGIN.class, signal -> {
            // 进入RUNNING状态，触发状态监听
            crntState.set(PopularState.RUNNING);
            infoReporter.resetInfo();
            return true;
        });

        pileUpSlot(DATA_TASK_END.class, signal -> {
            // 进入RUNNABLE状态，触发状态监听
            crntState.set(PopularState.RUNNABLE);
            return true;
        });

        infoReporter = createInfoReporter();
    }

    enum CoupledType {
        PARENT,
        SUBCLASS
    }
    private Class<? extends IEventProc>[] getCoupledProc(AbstractIntegratedProcessor processor, CoupledType type) {
        Processor annotation = processor.getClass().getAnnotation(Processor.class);
        if (annotation == null) {
            log.warn("The processor has no 'Processor' annotation!");
            return null;
        } else {
            return switch (type) {
                case PARENT -> annotation.coupledParents();
                case SUBCLASS -> annotation.coupledSubs();
            };
        }
    }

    @Override
    public final boolean validateAsOutput(@NonNull GNode nextNode) {
        return validateAsOutput((AbstractIntegratedProcessor) nextNode.getAttachment(PROCESSOR));
    }

    public boolean validateAsOutput(@NonNull AbstractIntegratedProcessor nextProcessor) {
        Class<? extends IEventProc>[] coupledParents = getCoupledProc(nextProcessor, CoupledType.PARENT);
        boolean res1 = false;
        for (var parent : coupledParents) {
            if (parent == null
                    || parent == Processor.DEFAULT_COUPLE || parent == this.getClass()) {
                res1 = true;
                break;
            }
        }
        Class<? extends IEventProc>[] coupledSubclazzs = getCoupledProc(this, CoupledType.SUBCLASS);
        boolean res2 = false;
        for (var sub : coupledSubclazzs) {
            if (sub == null
                    || sub == Processor.DEFAULT_COUPLE || sub == nextProcessor.getClass()) {
                res2 = true;
                break;
            }
        }
        return res1 & res2;
    }

    @Override
    public final boolean validateAsInput(@NonNull GNode parentNode) {
        return validateAsInput((AbstractIntegratedProcessor) parentNode.getAttachment(PROCESSOR));
    }

    public boolean validateAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        Class<? extends IEventProc>[] coupledParents = getCoupledProc(this, CoupledType.PARENT);
        boolean res1 = false;
        for (var parent : coupledParents) {
            if (parent == null
                    || parent == Processor.DEFAULT_COUPLE || parent == previousProcessor.getClass()) {
                res1 = true;
                break;
            }
        }
        boolean res2 = false;
        Class<? extends IEventProc>[] coupledSubclazzs = getCoupledProc(previousProcessor, CoupledType.SUBCLASS);
        for (var sub : coupledSubclazzs) {
            if (sub == null
                    || sub == Processor.DEFAULT_COUPLE || sub == this.getClass()) {
                res2 = true;
                break;
            }
        }
        return res1 & res2;
    }

    @Override
    public void connectedAsOutput(@NonNull GNode nextNode) {
        connectedAsOutput((AbstractIntegratedProcessor) nextNode.getAttachment(PROCESSOR));
    }

    public void connectedAsOutput(@NonNull AbstractIntegratedProcessor nextProcessor) {
        addNextProcessor(nextProcessor);
    }

    @Override
    public void connectedAsInput(@NonNull GNode parentNode) {
        connectedAsInput((AbstractIntegratedProcessor) parentNode.getAttachment(PROCESSOR));
    }

    public void connectedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        addPreviousProcessor(previousProcessor);
    }

    @Override
    public void detachedAsOutput(@NonNull GNode nextNode) {
        detachedAsOutput((AbstractIntegratedProcessor) nextNode.getAttachment(PROCESSOR));
    }

    public void detachedAsOutput(@NonNull AbstractIntegratedProcessor nextProcessor) {
        removeNextProcessor(nextProcessor);
    }

    @Override
    public void detachedAsInput(@NonNull GNode parentNode) {
        detachedAsInput((AbstractIntegratedProcessor) parentNode.getAttachment(PROCESSOR));
    }

    public void detachedAsInput(@NonNull AbstractIntegratedProcessor previousProcessor) {
        removePreviousProcessor(previousProcessor);
    }

    // for all nextProcessors
    public void disconnectAsOutput() {
        for (var proc : nextProcessors) {
            proc.detachedAsInput(this);
        }
    }
    // for all previousProcessors
    public void disconnectAsInput() {
        for (var proc : previousProcessors) {
            proc.detachedAsOutput(this);
        }
    }

    public void addPreviousProcessor(@NonNull AbstractIntegratedProcessor previousProcessor) {
        if (previousProcessors != null) {
            previousProcessors.add(previousProcessor);
        } else throw new ProcessorInitException("The 'previousProcessors' is null!");
    }

    public void removePreviousProcessor(@NonNull AbstractIntegratedProcessor previousProcessor) {
        if (previousProcessors != null) {
            previousProcessors.remove(previousProcessor);
        } else throw new ProcessorInitException("The 'previousProcessors' is null!");
    }

    public void addNextProcessor(@NonNull AbstractIntegratedProcessor nextProcessor) {
        if (nextProcessors != null) {
            nextProcessors.add(nextProcessor);
        } else throw new ProcessorInitException("The 'nextProcessors' is null!");
    }

    public void removeNextProcessor(@NonNull AbstractIntegratedProcessor nextProcessor) {
        if (nextProcessors != null) {
            nextProcessors.remove(nextProcessor);
        } else throw new ProcessorInitException("The 'nextProcessors' is null!");
    }

    public enum RoutingPattern implements ISignal.IRoutingPattern {

        /** 仅执行一次，即不往下级传递*/
        RECV_ONLY_ONCE(){
            public void route(IRouter<IEvent> proc, boolean sigState, ISignal signal) {
                // end 不路由
            }
        },
        /** 仅消费一次，即路由至信号处理完后{@link #transOrHandleSig(ISignal)}返回的结果为true，则不再往下级传递*/
        UNTIL_TRUE(){
            public void route(IRouter<IEvent> router, boolean sigState, ISignal signal) throws Exception {
                if(!sigState)
                    router.route(signal);
            }
        },
        /** 无论信号响应结果真假，都往下级传递*/
        ALWAYS_TRANSITIVE(){
            public void route(IRouter<IEvent> router, boolean sigState, ISignal signal) throws Exception {
                router.route(signal);
            }
        };

        RoutingPattern(){}
    }


    protected void routeSignal(boolean state, ISignal signal) throws Exception {
        signal.getRoutingPattern().route(this, state, signal);
    }

    /** 迭代所有子handler对该信号的处理，并将结果相与(&)*/
    protected boolean iterativeResponse(ISignal signal, IEventProc handler, boolean lastState) {
        if (handler == null)
            return lastState;

        lastState = doSlots(signal, handler, lastState);

        if (handler instanceof IDataTrans transHandler)
            return iterativeResponse(signal, transHandler.getNextHandler(), lastState);
        else
            return lastState;
    }

    private boolean doSlots(ISignal signal, IEventProc handler, boolean lastState) {
        Map<Class<? extends ISignal>, List<Function<ISignal, Boolean>>> slots = handler.getSlots();
        if (slots != null) {
            for (var entry : slots.entrySet()) {
                if (entry.getKey().isAssignableFrom(signal.getClass())) {
                    lastState &= entry.getValue().stream()
                            .map(action -> action.apply(signal)).reduce(true, (a, b) -> a && b);
                }
            }
        }
        return lastState;
    }

    /**
     * 当Porcessor接收到一个信号时，会遍历所有slots中的信号类型（键），只要该信号是当前类型的子类，则执行
     * Function<输入的信号, 信号是否响应成功>*/
    @Getter
    protected Map<Class<? extends ISignal>, List<Function<ISignal, Boolean>>> slots = new LinkedHashMap<>();;

    @Override
    public void pileUpSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot) {
        ProcessorManager.addSlot(slots, sig, false, slot);
    }

    @Override
    public void appendSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot) {
        ProcessorManager.addSlot(slots, sig, true, slot);
    }

    protected Map<IRouter, Integer> triggerConditions = new ConcurrentHashMap<>(); // 信号事件处理触发条件（抵达次数）
    public void clearTriggerConditions() {triggerConditions.clear();}

    protected Map<ISignal, Integer> sigArrivedCounts = new ConcurrentHashMap<>(); // 记录每个信号抵达计数

    // 作为源头processor，让子Handlers处理信号后路由信号
    protected void fireSignalAsSource(ISignal signal) throws Exception {
        routeSignal(activateHandlersSlots(signal, true), signal);
    }

    private void transOrHandleSig(ISignal signal) throws Exception {
        // processor不处理自己发出的信号
        // 信号响应结果
        boolean state = true;
        /** 输入的信号 => 信号是否响应成功 */
        boolean valid = checkTriggerCondition(signal);
        if (valid) { // 满足触发条件即可递归槽方法
            state = activateHandlersSlots(signal, true);
        }

        // 当前为末端processor
        if (endFlag && TASK_FINISH.class.isAssignableFrom(signal.getClass())) {
            if (valid) { // 将满足触发条件的终结信号闭环任务
                TaskManager.closeLoop(((TASK_FINISH) signal).getTask());
            }
        } else { // 非末端processor继续路由信号
            routeSignal(state, signal);
        }
    }
    /**
     *  同一个信号可能途径多个处理分支后到达当前节点，此时须确保是最后一次到达当前节点后再消费，
     * 才能确保信号处理的同步性和单一性
     * */
    private static final int TRIGGER_ONCE = 1;
    private boolean checkTriggerCondition(ISignal signal) {
        synchronized (signal) { // 防止不同处理器对同一个信号同时检查触发条件时冲突
            /** 不允许重复消费信号则第一次就处理*/
            if (!signal.isReconsumable()) {
                return true;
            }

            Integer triggerCondition = triggerConditions.computeIfAbsent(signal.getSource(),
                    k -> TaskManager.acquireLoopNumFrom(signal.getSource(), this));

            // 触发条件为1次，则直接执行
            if (triggerCondition == TRIGGER_ONCE) {
                return true;
            }

            if (triggerCondition > TRIGGER_ONCE) {
                return sigArrivedCounts.compute(signal, (k, v) -> {
                    if (v == null)
                        return 1;
                    v++;
                    return v >= triggerCondition ? null : v; // 满足触发条件 : 记录已到达的次数
                }) == null;
            }
            // 不可达的信号却到达了
            throw new RuntimeException("The processor is unreachable for the arrived signal!");
        }
    }

    private boolean activateHandlersSlots(ISignal signal, boolean state) {
        if (firstHandler == null)
            throw new ProcessorInitException("Hadn't set the firstHandler of processor!");
        // 先激活processor的槽，再递归子handler的槽
        if (firstHandler != this) {
            state = doSlots(signal, this, state);
        }
        state = iterativeResponse(signal, firstHandler, state);
        return state;
    }

    /** 在当前执行器（如果有）中处理信号，适用于外部调用，
     * 从而在{@link #transOrHandleSig}中触发父类槽，如触发监控和关闭监控等槽响应*/
    public void handleSigWithinExecutor(ISignal signal) throws Exception {
        if (!asynchronous || signal.isMandatorySync()) { //同步处理或强制同步信号
            transOrHandleSig(signal);
        } else {
            executor.execute(() -> {
                try {
                    transOrHandleSig(signal);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            });
        }
    }

    /** 客户端主动路由信号
     * PS: 慎用！因为调用它会跨过当前processor及其handlers的所有槽，
     * 可能会导致某些任务信号在特定情况下未闭环处理的问题，如处理链仅有一个节点时。
     * 建议使用{@link #transOrHandleSig(ISignal)}
     * */
    @Override
    public void route(IEvent signal) throws Exception {
        if (!endFlag) {
            for (AbstractIntegratedProcessor next: nextProcessors) {
                next.handle(signal);
            }
        }
    }

    /** 缓存{@value #BUFFERED_SIGNALS_AMOUNT}个信号
     * #- 未来可考虑过期处理 */
    private static final int BUFFERED_SIGNALS_AMOUNT = 40;
    private final EvictingQueue<Integer> receivedSignals = EvictingQueue.create(BUFFERED_SIGNALS_AMOUNT);

    /** 处理环回信号，默认不处理*/
    protected void handleLoopSignal(ISignal signal) {
        log.warn("{} get a loopback signal: {}", this.getNameValue(), signal.getName());
        // empty impl
    }

    /** 由信号的源头发起者捕获信号路由过程中的异常 */
    @Override
    public void handle(IEvent event) throws Exception {
        //TODO ASM织入方式 ?考虑是否在TERMINATED状态下不能传递事件
        /** ps: 平台须确保在切换至[编辑模式]时将所有处理机状态置为TERMINATED，
         * 故这里可保证[编辑模式]下不处理信号 */
        if (condition == _TERMINATED)
            return;

        if (event instanceof ISignal signal) {
            // 处理环回信号
            if (signal.getSource().equals(this)) {
                handleLoopSignal(signal); // 默认不处理
                return;
            }
            // 处理重复信号
            if (!signal.isReconsumable()) { // 当前信号不允许重复处理
                synchronized (procMutex) {
                    if (receivedSignals.contains(signal.getNum())) // 重复即丢弃
                        return;
                    else
                        receivedSignals.add(signal.getNum());
                }
            }

            handleSigWithinExecutor(signal);

        } else {
            log.error("Unsupported event type!");
            throw new Exception("Unsupported event type!");
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append("(name: ").append(this.getName());
                if (this.getProcessorId() != null)
                    sb.append(", pID: ").append(this.getProcessorId().toString());
                sb.append(")");
        return sb.toString();
    }

    public String getMsgInfo(IEvent event){
        StringBuilder sb = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append("(name: ").append(this.getName());
                if (this.getProcessorId() != null)
                    sb.append(", pID: ").append(this.getProcessorId().toString());
                sb.append(")").append(NEWLINE);
        event.appendDumpInfo(sb);
        return sb.toString();
    }

    /** 所有子处理器集合
     * ps：
     * 1. 引入子处理器时，通过调用{@link AbstractHandler#setParentProcessor}方法将其加入{@link #subHandlers}中
     * 2. 删除任意子处理器时，注意通过{@link AbstractHandler#getParentProcessor()}
     * 或{@link AbstractHandler#removeFromParent()}方法将其从{@link #subHandlers}中移除，并且执行其clear()方法
     * 如在前端界面“-”删除单个Handler。
     * 3. 目前删除整个父综合处理器时（调用{@link #clear()}）可以不清空{@link #subHandlers}，但须执行其中每一个handler
     * 的clear()方法。
     * */
    @Getter
    protected Set<AbstractHandler> subHandlers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 所拥有的handler在删除时须做出响应动作的属性
     * 初始为null，添加时若由界面化或反序列化创建时，会在扫描自身所有属性的同时创建该列表，并将实现了
     * {@link IDeletionResponder}接口的字段都添加进去，所以在删除该processor时可直接遍历执行{@link IDeletionResponder#beforeDeletion()}
     * 若在删除时调用{@link #clear()}中发现该列表为null，则重新扫描。
     * */
    @Getter
    protected Set<IDeletionResponder> deletionFields;
    /** 遍历所有子Handler并清理*/
    private void iterativeClearing(IEventProc handler) {
        if (handler == null)
            return;
        if (handler instanceof AbstractHandler handlerToClear)
            handlerToClear.clear();
        if (handler instanceof IDataTrans transHandler) {
            iterativeClearing(transHandler.getNextHandler());
        }
    }

    private boolean cleared = false;
    public void clear() {
        synchronized (procMutex) {
            if (cleared)
                throw new RuntimeException("This processor had been cleared!");
            // 归还PId
            ProcessorManager.removeProcessorId(processorId);

            /** 清理所包含的子处理器*/
            if (firstHandler == null)
                throw new ProcessorInitException("Hadn't set the firstHandler of processor!");
            iterativeClearing(firstHandler);

            /** {@link #deletionFields} 为null，说明当前processor尚未被扫描过{@link IDeletionResponder}*/
            if (deletionFields == null) {
                Class<?> clazz = this.getClass();
                while (clazz != null && clazz != Object.class) {
                    Arrays.stream(clazz.getDeclaredFields())
                            .filter(e -> IDeletionResponder.class.isAssignableFrom(e.getType()))
                            .forEach(e -> {
                                e.setAccessible(true);
                                try {
                                    ((IDeletionResponder) e.get(AbstractIntegratedProcessor.this)).beforeDeletion();
                                } catch (IllegalAccessException illegal) {
                                    log.error(illegal.getMessage());
                                }
                            });
                    if (clazz != AbstractIntegratedProcessor.class)
                        clazz = clazz.getSuperclass();
                    else
                        break;
                }
            } else {
                deletionFields.forEach(IDeletionResponder::beforeDeletion);
            }
            cleared = true;
        }
    }

    public void reset0(WorkMode oriMode, WorkMode targetMode) {
        receivedSignals.clear();
        /** 不能在此处清理{@value #sigArrivedCounts#triggerConditions}，因为后续可能还需要处理事件
         * ，故应在切换至编辑模式处清理 */
        reset(oriMode, targetMode);
    }

    public abstract void reset(WorkMode oriMode, WorkMode targetMode);

    @Getter
    protected boolean sourceFlag = true;
    @Getter
    protected boolean endFlag = true;
    public void boot() throws Exception {
        // 启动所有子handler
        if (firstHandler == null)
            throw new ProcessorInitException("Hadn't set the firstHandler of processor!");
        iterativeBooting(firstHandler);
        // 判断是否为源头processor
        if (previousProcessors.isEmpty()) {
            sourceFlag = true;
            ProcessorManager.addSource(this);
        } else {
            sourceFlag = false;
        }
        // 判断是否为末端processor
        endFlag = nextProcessors.isEmpty();
    }
    /** 遍历所有子Handler并启动*/
    private void iterativeBooting(IEventProc handler) {
        if (handler == null)
            return;
        if (handler instanceof AbstractHandler handlerToBoot)
            handlerToBoot.boot();
        if (handler instanceof IDataTrans transHandler) {
            iterativeBooting(transHandler.getNextHandler());
        }
    }

    @Getter @Setter
    private Future<?> closingFuture = null;

    @Getter
    protected AbstractInfoReporter infoReporter = null;

    abstract protected AbstractInfoReporter createInfoReporter();

    @Getter @Setter
    protected BaseCustomisedChangeListener<PopularState> crntStateListener = null;

    @Override
    public void customise(Set<Node> controllers) {
        ProcessorHelper.customise(this, controllers);
    }

    @Override
    public void uncustomize(Set<Node> controllers) {
        ProcessorHelper.uncustomize(this, controllers);
    }
}
