package sy.databus.process;

import javafx.scene.Node;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.databus.entity.property.IDeletionResponder;
import sy.databus.entity.signal.ISignal;
import sy.databus.global.WorkMode;
import sy.databus.organize.BaseCustomisedChangeListener;
import sy.databus.organize.ComponentCustomization;
import sy.databus.organize.ProcessorManager;
import sy.databus.process.frame.handler.Handler;
import sy.databus.process.fsm.PopularState;
import sy.databus.process.fsm.SyncObservableState;

import java.util.*;
import java.util.function.Function;

import static sy.databus.process.Console.Config.STATIC;

@Log4j2
public abstract class AbstractHandler<T> implements IEventProc<T>, ComponentCustomization {
    /** 该id等于当前handler在其综合处理器中的hanlder列表中的索引位置*/
    protected byte id;

    @Getter
    @Setter
    @Console(config = STATIC, display = "名称")
    protected String name = Handler.DEFAULT_NAME;

    @Getter
    protected AbstractIntegratedProcessor parentProcessor; //引用父处理器容器
    public void setParentProcessor(AbstractIntegratedProcessor parentProcessor) {
        this.parentProcessor = parentProcessor;
        synchronized (this.parentProcessor.getProcMutex()) {
            this.parentProcessor.getSubHandlers().add(this);
        }
    }
    public void removeFromParent() {
        synchronized (this.parentProcessor.getProcMutex()) {
            this.parentProcessor.getSubHandlers().remove(this);
        }
    }

    /** 所拥有的所有在删除时须做出响应动作的属性
     * 初始为null，添加时若由界面化或反序列化创建时，会在扫描自身所有属性的同时创建该列表，并将实现了
     * {@link IDeletionResponder}接口的字段都添加进去，所以在删除该handler时可直接遍历执行{@link IDeletionResponder#beforeDeletion()}
     * 若在删除时调用{@link #clear()}中发现该列表为null，则重新扫描。
     * */
    @Getter
    protected Set<IDeletionResponder> deletionFields;

    private boolean cleared = false;
    public void clear() {
        Object mutex = this.parentProcessor.getProcMutex();
        if (mutex == null) {
            log.warn("A handler without parent processor is clearing!");
            mutex = this;
        }
        synchronized (mutex) {
            if (cleared)
                throw new RuntimeException("This handler had been cleared!");
            /** {@link #deletionFields} 为null，说明当前processor尚未被扫描过{@link IDeletionResponder}*/
            if (deletionFields == null) {
                Class<?> clazz = this.getClass();
                while (clazz != null && clazz != Object.class) {
                    Arrays.stream(clazz.getDeclaredFields())
                            .filter(e -> IDeletionResponder.class.isAssignableFrom(e.getType()))
                            .forEach(e -> {
                                e.setAccessible(true);
                                try {
                                    ((IDeletionResponder) e.get(AbstractHandler.this)).beforeDeletion();
                                } catch (IllegalAccessException illegalAccessException) {
                                    illegalAccessException.printStackTrace();
                                }
                            });
                    if (clazz != AbstractHandler.class)
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

    abstract public void initialize();

    public void boot() {
        // impl by subclass in necessary
    }

    public void reset(WorkMode oriMode, WorkMode targetMode) {
        // impl by subclass in necessary
    }

    @Getter
    /** Function<输入的信号, 信号是否响应成功>
     * 使用有序Map，使父类的槽先执行*/
    protected Map<Class<? extends ISignal>, List<Function<ISignal, Boolean>>> slots = new LinkedHashMap<>();;

    @Override
    public void pileUpSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot) {
        ProcessorManager.addSlot(slots, sig, false, slot);
    }

    @Override
    public void appendSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot) {
        ProcessorManager.addSlot(slots, sig, true, slot);
    }

    @Override
    public void customise(Set<Node> controllers) {
        ProcessorHelper.customise(parentProcessor, controllers);
    }

    @Override
    public void uncustomize(Set<Node> controllers) {
        ProcessorHelper.uncustomize(parentProcessor, controllers);
    }

    @Override
    public SyncObservableState getCrntState() {
        return parentProcessor.getCrntState();
    }

    @Override
    public BaseCustomisedChangeListener<PopularState> getCrntStateListener() {
        return parentProcessor.getCrntStateListener();
    }

    @Override
    public void setCrntStateListener(BaseCustomisedChangeListener<PopularState> listener) {
        parentProcessor.setCrntStateListener(listener);
    }
}
