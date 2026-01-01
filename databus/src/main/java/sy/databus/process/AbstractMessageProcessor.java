package sy.databus.process;

import javafx.collections.ListChangeListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.databus.entity.IEvent;
import sy.databus.entity.ProcessorId;
import sy.databus.entity.message.IMessage;

import sy.databus.global.WorkMode;
import sy.databus.organize.monitor.AbstractInfoReporter;
import sy.databus.organize.monitor.DefaultMsgProcessorInfoReporter;
import sy.databus.process.frame.AbstractTransHandler;
import sy.databus.process.frame.MessageSeriesProcessor;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.netty.util.internal.StringUtil.NEWLINE;
import static sy.databus.process.Console.Category.PRE_HANDLERS;
import static sy.databus.process.Console.Category.SERVICE_HANDLERS;
import static sy.databus.process.Console.Config.DYNAMIC;
import static sy.databus.process.Console.Config.STATIC;

/**
 * firstHandler ->
 * 拥有内部处理器集合的子类，初始化后须调用{@link #installInternalHandlers()}方法
 * 拥有预处理器集合的子类，初始化后须调用{@link #installPreHandlers()}方法
 * copier ->
 * 拥有业务处理器集合的子类，初始化后须调用{@link #installServiceHandlers()}方法
 * tailHandler
 *
 * ps:
 *  1. 若子类在{@link #initialize()}方法中改变了端点处理器（copier, tailHandler），则须重新调用上述方法。
 *  例如：{@link LSNFileReplayer}
 *  2. 由于XXXDevSelector中没有Copier，而MessageSeriesProcessor中有Copier，所以它们对handle方法的实现不同。
 * */

@Log4j2
public abstract class AbstractMessageProcessor<T> extends AbstractIntegratedProcessor implements IDataTrans<IMessage<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMessageProcessor.class);

    // TODO 考虑多线程对统计结果影响（asynchronous为false时考虑使用原子变量）
    @Getter @Setter
    protected volatile int inPkgCount = 0;
    @Getter @Setter
    protected volatile int outPkgCount = 0;
    @Getter @Setter
    protected volatile long inBytes = 0;
    @Getter @Setter
    protected volatile long outBytes = 0;

    @Setter @Getter
    @Console(config = DYNAMIC, display = "工作")
    protected volatile boolean passable = true;

    public AbstractMessageProcessor(){}

    public AbstractMessageProcessor(ProcessorId id) {
        super(id);
    }

    @Override
    public void initialize() {
        super.initialize();

        installInternalHandlers();
    }

    @Override
    public void setNextHandler(IEventProc<IMessage<T>> nextHandler) {
        firstHandler = nextHandler;
    }

    @Override
    public IEventProc<IMessage<T>> getNextHandler() {
        return firstHandler;
    }

    @Override
    public void fireNext(IMessage<T> msg) throws Exception {
        firstHandler.handle(msg);
    }

    @Getter @Setter
    protected SSyncObservableList<AbstractTransHandler> internalHandlers;

    @Getter @Setter
    @Console(config = STATIC, category = PRE_HANDLERS)
    protected SSyncObservableList<AbstractTransHandler> preHandlers; // 预处理器

    /** 每个消息综合处理器中有则仅有一个非空的拷贝器*/
    @Getter
    protected AbstractTransHandler<IMessage<T>> copier; // 拷贝器
    protected void setCopier(AbstractTransHandler<IMessage<T>> copier) {
        this.copier = copier;
        this.copier.setParentProcessor(this);
        connectAllHandlers();
    }
    protected Copier DEFAULT_COPIER = new Copier();

    @Getter @Setter
    @Console(config = STATIC, category = SERVICE_HANDLERS)
    protected SSyncObservableList<AbstractTransHandler> serviceHandlers; // 业务处理器

    protected OutBoundHandler DEFAULT_TAIL = new OutBoundHandler();
    @Getter
    private AbstractHandler<IMessage<T>> tailHandler = DEFAULT_TAIL; // 出站处理器
    /**
     * ps: 使用{@link #setCopier(AbstractTransHandler)}和{@link #setTailHandler(AbstractHandler)}时，
     * 须参考{@link #DEFAULT_COPIER}和{@link #DEFAULT_TAIL}，考虑消息拷贝机制的设计
     * */
    protected void setTailHandler(AbstractHandler<IMessage<T>> tailHandler) {
        this.tailHandler = tailHandler;
        this.tailHandler.setParentProcessor(this);
        connectAllHandlers();
    }

    // 入站统计，输入++
    protected void inBoundedStatistic(IMessage<T> msg) {
        inPkgCount++;
        inBytes += msg.dataCapacity();
    }

    // 出站统计，输出++
    protected void outBoundedStatistic(IMessage<T> msg) {
        outPkgCount++;
        outBytes += msg.dataCapacity();
    }

    protected void handleExceptionMsg(IMessage<T> exceptionMsg) {
        // #- log;
        LOGGER.error("帧处理器中发现异常消息：" + NEWLINE + "{}", this.getMsgInfo(exceptionMsg));
        exceptionMsg.clear();
//        close();
    }

    protected void undertakeInQueue(IMessage<T> msg) {
        inBoundedStatistic(msg);
        try {
            copier.getNextHandler().handle(msg);
        } catch (Exception e) {
            log.error(e);
            handleExceptionMsg(msg);
        }
    }

    @Override
    public void handle(IEvent event) throws Exception {
        if (event instanceof IMessage msg) {
            try{
                if (!passable) {
                    msg.release();
                    return;
                }
                fireNext(msg);
            } catch (Exception e) {
                log.error(e);
                handleExceptionMsg(msg);
            }
        } else {
            super.handle(event);
        }
    }

    protected class Copier extends AbstractTransHandler<IMessage<T>> {

        public Copier(){
            setParentProcessor(AbstractMessageProcessor.this);
        }

        private void dispose(IMessage<T> msg) throws Exception {
            if (!asynchronous) {
                inBoundedStatistic(msg);
                nextHandler.handle(msg);
            } else {
                // 异步执行
                executor.execute(new InterruptHandler() {
                    @Override
                    public void terminated() {
                        msg.clear();
                    }

                    @Override
                    public void run() {
                        undertakeInQueue(msg);
                    }
                });
            }
        }

        @Override
        public void handle(IMessage<T> msg) throws Exception {
            if(msg.getFutureCopy()) {
                /**
                 * 由于在{@link MessageSeriesProcessor.OutBoundHandler}
                 * 中出站时调用了msg.retain()，创建了“副本”（增加了引用计数）
                 * 所以这里复制完成前需要release()一次，销毁副本
                 * */
                msg.release();
                IMessage<T> newMsg = msg.copy();
                dispose(newMsg);
            }else{
                dispose(msg);
            }
        }

        @Override
        public void initialize() {
            this.name = "copier";
        }

    }

    protected class OutBoundHandler extends AbstractTransHandler<IMessage<T>> {

        OutBoundHandler() {
            setParentProcessor(AbstractMessageProcessor.this);
        }

        @Override
        public void handle(IMessage<T> msg) throws Exception{
            // #- 后期考虑对nextProcessors的操作加锁
            if(endFlag){
                // #- log 下一级处理器未加入或全部被移除
                msg.release();
            }
            else for(int i = 0, size = nextProcessors.size(); i < size; i++){
                if(i == size - 1) msg.setFutureCopy(false);
                else{
                    msg.setFutureCopy(true);
                    msg.retain();// 本质上为消息的retainedDuplicate
                }
                outBoundedStatistic(msg);
                nextProcessors.get(i).handle(msg);
            }
        }

        @Override
        public void initialize() {
            this.name = "TailHandler";
            this.id = (byte) 0xff;
        }
    }

    private void connectHandlers(SSyncObservableList<AbstractTransHandler> handlers,
                                 EndpointHandlerPossibility startPos) {
        IDataTrans beginningHandler = determineBeginning(startPos);
        IEventProc endedHandler = determineEnding(startPos);
        if (handlers != null && handlers.size() > 0) {
            for (int i = 0; i < handlers.size(); i++) {
                // 设置父综合处理器
                handlers.get(i).setParentProcessor(this);
                // 连接内部
                if (i < handlers.size() - 1) {
                    handlers.get(i).setNextHandler(handlers.get(i + 1));
                }
            }

            beginningHandler.setNextHandler(handlers.get(0));
            handlers.get(handlers.size() - 1).setNextHandler(endedHandler);
        } else {
            beginningHandler.setNextHandler(endedHandler);
        }
    }

    /** 连接各internalHandler处理器 firstHandler - > internalHandlers -> preHandlers/...*/
    private AtomicBoolean joinedInternalHandlers = new AtomicBoolean(false);
    protected void installInternalHandlers() {
        if (joinedInternalHandlers.compareAndSet(false, true)) {
            if (internalHandlers == null)
                internalHandlers = new SSyncObservableList<>(procMutex);
            else internalHandlers.setMutex(procMutex); // < - 无参构造器反序列化processor后internalHandlers未绑定procMutex
            joinToInternalHandlers();
        }
        connectAllHandlers();
    }

    /** 连接各preHandler处理器 firstHandler/internalHandlers -> preHandlers -> copier/...*/
    private AtomicBoolean joinedPreHandlers = new AtomicBoolean(false);
    protected void installPreHandlers() {
        if (joinedPreHandlers.compareAndSet(false, true)) {
            if (preHandlers == null)
                preHandlers = new SSyncObservableList<>(procMutex);
            else preHandlers.setMutex(procMutex); // < - 无参构造器反序列化processor后preHandlers未绑定procMutex
            joinToPreHandlers();
        }
        connectAllHandlers();
    }

    /** 连接各Appender处理器 copier/... -> serviceHandlers -> tailHandler*/
    private AtomicBoolean joinedServiceHandlers = new AtomicBoolean(false);
    protected void installServiceHandlers() {
        if (joinedServiceHandlers.compareAndSet(false, true)) {
            if (serviceHandlers == null)
                serviceHandlers = new SSyncObservableList<>(procMutex);
            else serviceHandlers.setMutex(procMutex); // < - 无参构造器反序列化processor后serviceHandlers未绑定procMutex
            joinToServiceHandlers();
        }
        connectAllHandlers();
    }

    protected void connectAllHandlers() {
        if (internalHandlers != null) connectHandlers(internalHandlers, EndpointHandlerPossibility.INTERNAL_HANDLERS);
        if (preHandlers != null) connectHandlers(preHandlers, EndpointHandlerPossibility.PRE_HANDLERS);
        if (serviceHandlers != null) connectHandlers(serviceHandlers, EndpointHandlerPossibility.SERVICE_HANDLERS);
    }

    /** 添加internalHandler处理器*/
    public void addInternalHandler(int index, @NonNull AbstractTransHandler<? extends IMessage<T>> internalHandler) {
        if (internalHandlers == null)
            throw new ProcessorInitException("internalHandlers of " + this.getClass().getSimpleName() + " is null!");

        internalHandlers.add(index, internalHandler); // SSyncObservableList已加锁
    }

    public void removeInternalHandler(int index) {
        if (internalHandlers == null)
            throw new ProcessorInitException("internalHandlers of " + this.getClass().getSimpleName() + " is null!");

        if (internalHandlers.size() == 0)
            return;

        internalHandlers.remove(index);
    }

    /** 追加internalHandler处理器*/
    public void appendInternalHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> internalHandler) {
        if (internalHandlers == null)
            throw new ProcessorInitException("internalHandlers of " + this.getClass().getSimpleName() + " is null!");

        internalHandlers.add(internalHandler); // SSyncObservableList已加锁
    }

    public void removeInternalHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> internalHandler) {
        if (internalHandlers == null)
            throw new ProcessorInitException("internalHandlers of " + this.getClass().getSimpleName() + " is null!");

        internalHandlers.remove(internalHandler);
    }

    public boolean ofInternalHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (internalHandlers == null)
            return false;

        return internalHandlers.contains(handler);
    }

    public int indexOfInternalHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (internalHandlers == null)
            return -1;

        return internalHandlers.indexOf(handler);
    }

    /** 添加preHandler处理器*/
    public void addPreHandler(int index, @NonNull AbstractTransHandler<? extends IMessage<T>> preHandler) {
        preHandlers.add(index, preHandler); // SSyncObservableList已加锁
    }

    /** 追加preHandler处理器*/
    public void appendPreHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> preHandler) {
        preHandlers.add(preHandler); // SSyncObservableList已加锁
    }

    public void removePreHandler(int index) {
        if (preHandlers == null)
            throw new ProcessorInitException("preHandlers of " + this.getClass().getSimpleName() + " is null!");

        if (preHandlers.size() == 0)
            return;

        preHandlers.remove(index);
    }

    public void removePreHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> preHandler) {
        if (preHandlers == null)
            throw new ProcessorInitException("preHandlers of " + this.getClass().getSimpleName() + " is null!");

        preHandlers.remove(preHandler);
    }

    public boolean ofPreHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (preHandlers == null)
            return false;

        return preHandlers.contains(handler);
    }

    public int indexOfPreHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (preHandlers == null)
            return -1;

        return preHandlers.indexOf(handler);
    }

    /** 添加业务处理器*/
    public void addServiceHandler(int index, @NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        serviceHandlers.add(index, handler); // SSyncObservableList已加锁
    }

    /** 追加业务处理器*/
    public void appendServiceHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        serviceHandlers.add(handler); // SSyncObservableList已加锁
    }

    public void removeServiceHandler(int index) {
        if (serviceHandlers == null)
            throw new ProcessorInitException("serviceHandlers of " + this.getClass().getSimpleName() + " is null!");

        if (serviceHandlers.size() == 0)
            return;

        serviceHandlers.remove(index);
    }

    public void removeServiceHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> serviceHandler) {
        if (serviceHandlers == null)
            throw new ProcessorInitException("serviceHandlers of " + this.getClass().getSimpleName() + " is null!");

        serviceHandlers.remove(serviceHandler);
    }

    public boolean ofServiceHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (serviceHandlers == null)
            return false;

        return serviceHandlers.contains(handler);
    }

    public int indexOfServiceHandlers(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (serviceHandlers == null)
            return -1;

        return serviceHandlers.indexOf(handler);
    }

    public void removeHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> handler) {
        if (ofInternalHandlers(handler)) {
            removeInternalHandler(handler);
        } else if (ofPreHandlers(handler)) {
            removePreHandler(handler);
        } else if (ofServiceHandlers(handler)){
            removeServiceHandler(handler);
        } else {
            log.warn("This handler is not belong to the processor!");
        }
    }

    public int insertHandler(@NonNull AbstractTransHandler<? extends IMessage<T>> target,
                              @NonNull AbstractTransHandler<? extends IMessage<T>> behind) {
        int targetPos = -1;
        targetPos = indexOfPreHandlers(target);
        if (targetPos >= 0) {
            addPreHandler(targetPos + 1, behind);
            return targetPos;
        }
        targetPos = indexOfServiceHandlers(target);
        if (targetPos >= 0) {
            addServiceHandler(targetPos + 1, behind);
            return targetPos;
        }
        targetPos = indexOfInternalHandlers(target);
        if (targetPos >= 0) {
            addInternalHandler(targetPos + 1, behind);
            return targetPos;
        }
        return -1;
    }

    @FunctionalInterface
    interface Beginning {
        IDataTrans asBeginningHandler(AbstractMessageProcessor processor);
    }
    @FunctionalInterface
    interface Ending {
        IEventProc asEndedHandler(AbstractMessageProcessor processor);
    }

    enum EndpointHandlerPossibility {
        ITSELF(
                processor -> processor,
                processor -> {
                    throw new ProcessorInitException("'ITSELF' cannot be ended handler!");
                }
        ),

        INTERNAL_HANDLERS(
                processor -> {
                    if (processor.internalHandlers != null && processor.internalHandlers.size() > 0) {
                        return (IDataTrans) processor.internalHandlers.get(processor.internalHandlers.size() - 1);
                    } else return null;
                },
                processor -> {
                    throw new ProcessorInitException("'INTERNAL_HANDLERS' cannot be ended handler!");
                }
        ),

        PRE_HANDLERS(
                processor -> {
                    if (processor.preHandlers != null && processor.preHandlers.size() > 0) {
                        return (IDataTrans) processor.preHandlers.get(processor.preHandlers.size() - 1);
                    } else return null;
                },
                processor -> {
                    if (processor.preHandlers != null && processor.preHandlers.size() > 0) {
                        return (IEventProc) processor.preHandlers.get(0);
                    } else return null;
                }
        ),

        COPIER(
                processor -> {
                    if (processor.copier != null)
                        return processor.copier;
                    else return null;
                },
                processor -> {
                    if (processor.copier != null)
                        return processor.copier;
                    else return null;
                }
        ),

        SERVICE_HANDLERS(
                processor -> {
                    if (processor.serviceHandlers != null && processor.serviceHandlers.size() > 0) {
                        return (IDataTrans) processor.serviceHandlers.get(processor.serviceHandlers.size() - 1);
                    } else return null;
                },
                processor -> {
                    if (processor.serviceHandlers != null && processor.serviceHandlers.size() > 0) {
                        return (IEventProc) processor.serviceHandlers.get(0);
                    } else return null;
                }
        ),

        TAIL_HANDLER(
                processor -> {
                    throw new ProcessorInitException("'TAIL_HANDLER' cannot be beginning handler!");
                },
                processor -> {
                    if (processor.tailHandler != null)
                        return processor.tailHandler;
                    else return null;
                }
        );

        @Getter
        Beginning beginning;
        @Getter
        Ending ending;

        EndpointHandlerPossibility(Beginning beginning, Ending ending) {
            this.beginning = beginning;
            this.ending = ending;
        }
    }

    private IDataTrans determineBeginning(EndpointHandlerPossibility startPos) {
        for (int i = startPos.ordinal() - 1; i >= 0 ; i--) {
            IDataTrans beginner = EndpointHandlerPossibility.values()[i]
                    .getBeginning().asBeginningHandler(this);
            if (beginner != null)
                return beginner;
        }

        throw new RuntimeException("Cannot determine the beginner by the '" + startPos.name() + "'!");
    }

    private IEventProc determineEnding(EndpointHandlerPossibility startPos){
        for (int i = startPos.ordinal() + 1; i < EndpointHandlerPossibility.values().length; i++) {
            IEventProc ending = EndpointHandlerPossibility.values()[i]
                    .getEnding().asEndedHandler(this);
            if (ending != null)
                return ending;
        }

        throw new RuntimeException("Cannot determine the ending by the '" + startPos.name() + "'!");
    }

    private void joinToHandlers(SSyncObservableList<AbstractTransHandler> handlers,
                                EndpointHandlerPossibility startPos) {

        handlers.addListener((ListChangeListener<? super AbstractTransHandler>) change -> {
            /** 目前业务处理器不支持在非编辑模式下重连接*/
/*            if (handlers == serviceHandlers && !GlobalState.currentWorkMode.get().equals(WorkMode.EDIT))
                return;*/

            // 确定首部处理器
            IDataTrans beginningHandler = determineBeginning(startPos);
            // 确定尾部处理器
            IEventProc endedHandler = determineEnding(startPos);

            onHandlersChanged(change, beginningHandler, endedHandler);
        });
    }

    private void joinToInternalHandlers() {
        joinToHandlers(internalHandlers, EndpointHandlerPossibility.INTERNAL_HANDLERS);
    }

    private void joinToPreHandlers() {
        joinToHandlers(preHandlers, EndpointHandlerPossibility.PRE_HANDLERS);
    }

    private void joinToServiceHandlers() {
        joinToHandlers(serviceHandlers, EndpointHandlerPossibility.SERVICE_HANDLERS);
    }

    protected void onHandlersChanged(ListChangeListener.Change<? extends AbstractTransHandler> change,
                                     IDataTrans beginningHandler, IEventProc endedHandler) {
        while (change.next()) {
            /** {@value preHandlers}和{@value serviceHandlers}的各操作已被{@value procMutex} 同步
             *  故这里监听后的操作无需再加锁}
             * */
            if (change.wasAdded()) { // 添加Handler
                if (change.getAddedSize() == 1) {
                    AbstractTransHandler<IMessage<T>> handler = change.getAddedSubList().get(0);
                    //->判断是否是move行为触发的---------------------------------------------------
                    SSyncObservableList obList = (SSyncObservableList) change.getList();
                    if (change.getFrom() != obList.getMovedTarget()) {
                        //设置父综合处理器为当前processor
                        handler.setParentProcessor(this);
                    }
                    //<---------------------------------------------------------------------------
                    /** 必须先引用下一级Handler，否则可能会导致空指针异常*/
                    // change.getList()得到的是增删之后的list
                    // list.add(index, o) 会将原index位置上的对象挪到index + 1位置，然后将o放到index位置
                    // index值即为getFrom()
                    if (change.getFrom() == change.getList().size() - 1) {
                        // 添加在尾部（即list.add(o)），下一级Handler为tailHandler
                        // 此时change.getList().size()得到的是添加后的size，所以-1
                        handler.setNextHandler(endedHandler);
                    } else { // 添加在中间
                        AbstractTransHandler<IMessage<T>> nextHandler = change.getList().get(change.getFrom() + 1);
                        handler.setNextHandler(nextHandler);
                    }
                    /** 再引用上一级Handler*/
                    if (change.getFrom() == 0) { // 添加在头部，上一级Handler为headHandler/copier
                        beginningHandler.setNextHandler(handler);
                    } else { // 添加在中间
                        AbstractTransHandler<IMessage<T>> nextHandler = change.getList().get(change.getFrom() - 1);
                        nextHandler.setNextHandler(handler);
                    }
                } else {
                    LOGGER.error("HandlerList of {} only support add one Handler once!",
                            MessageSeriesProcessor.class.getSimpleName());
                }
            } else if (change.wasRemoved()) {
                if (change.getRemovedSize() == 1) {
                    //->判断是否是move行为触发的---------------------------------------------------
                    SSyncObservableList obList = (SSyncObservableList) change.getList();
                    if (change.getFrom() != obList.getMovedSource()) {
                        AbstractHandler handler = change.getRemoved().get(0);
                        handler.clear();
                        handler.removeFromParent();
                    }
                    //<---------------------------------------------------------------------------
                    if (change.getList().size() == 0) { // List中没有Handler了
                        beginningHandler.setNextHandler(endedHandler);
                    } else {
                        if (change.getFrom() == change.getList().size()) { // 删除发生在尾部，且留有Handler
                            AbstractTransHandler<IMessage<T>> previousHandler = change.getList().get(change.getFrom() - 1);
                            previousHandler.setNextHandler(endedHandler);
                        } else if (change.getFrom() == 0) { // 删除发生在头部，且留有Handler
                            beginningHandler.setNextHandler(change.getList().get(0));
                        } else { // 删除发生在中间
                            AbstractTransHandler<IMessage<T>> previousHandler = change.getList().get(change.getFrom() - 1);
                            AbstractTransHandler<IMessage<T>> nextHandler = change.getList().get(change.getFrom());
                            previousHandler.setNextHandler(nextHandler);
                        }
                    }
                } else {
                    LOGGER.error("HandlerList of {} only support remove one Handler once!",
                            MessageSeriesProcessor.class.getSimpleName());
                }
            }
        }
    }

    @Override
    public void reset(WorkMode oriMode, WorkMode targetMode) {
        infoReporter.resetInfo();
    }

    @Override
    protected AbstractInfoReporter createInfoReporter() {
        return new DefaultMsgProcessorInfoReporter(this);
    }
}
