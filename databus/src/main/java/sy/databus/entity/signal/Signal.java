package sy.databus.entity.signal;

import lombok.Getter;
import lombok.SneakyThrows;
import sy.databus.entity.IEvent;
import sy.databus.process.IEventProc;
import sy.databus.process.IRouter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Signal implements ISignal {
    static AtomicInteger sigSum = new AtomicInteger(0);

    @Getter
    private final IRouter<IEvent> source;

    // 信号附件
    private Map<String, Object> attachments;

    public Object getAttachment(String key) {
        if (attachments == null)
            return null;
        return attachments.get(key);
    }

    public void putAttachment(String key, Object value) {
        Objects.requireNonNull(key, "A null key is putting into attachments!");
        Objects.requireNonNull(value, "A null value is putting into attachments!");
        if (attachments == null)
            attachments = new HashMap<>();
        attachments.put(key, value);
    }

    protected IRoutingPattern routingPattern; // 一般默认方式为仅执行一次

    public enum Feature {

    }

    protected ArrayList<Feature> features;

    public ISignal addFeature(Feature e) {
        if (features == null)
            features = new ArrayList<>();
        features.add(e);
        return this;
    }

    public boolean containsFeature(Feature e) {
        if (features == null)
            return false;
        return features.contains(e);
    }

    // 该有参构造器是Builder的基础
    protected Signal(IRouter<IEvent> source, IRoutingPattern routing) {
        this.source = source;
        this.routingPattern = routing;
        this.num = sigSum.getAndIncrement();
    }
    // 信号序号， 可用来防止重复响应
    private final int num;
    @Override
    public int getNum() {
        return num;
    }

    public static class Builder {
        @SneakyThrows
        public static Signal newSignalInstance(IRouter<IEvent> source,
                                               Class<? extends Signal> clazz,
                                               IRoutingPattern routing) {
            return clazz.getDeclaredConstructor(IRouter.class, IRoutingPattern.class, int.class)
                    .newInstance(source, routing);
        }

        public static int getUniqueSigNum() {
            return sigSum.getAndIncrement();
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public IRoutingPattern getRoutingPattern() {return this.routingPattern;}

    /** 是否强制同步传递（默认false）*/
    private boolean mandatorySync = false;
    @Override
    public boolean isMandatorySync() {
        return mandatorySync;
    }
    public ISignal withMandatorySync(boolean flag) {
        this.mandatorySync = flag;
        return this;
    }

    /** 是否允许重复响应（默认true）*/
    private boolean reconsumable = true;
    @Override
    public boolean isReconsumable() {
        return reconsumable;
    }
    public ISignal withReconsumable(boolean flag) {
        this.reconsumable = flag;
        return this;
    }

    @Override
    public void setRoutingPattern(IRoutingPattern router) {
        this.routingPattern = router;
    }

    @Override
    public void appendDumpInfo(StringBuilder sb) {
        sb.append(getName());
    }

}
