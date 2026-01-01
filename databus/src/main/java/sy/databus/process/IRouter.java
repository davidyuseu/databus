package sy.databus.process;

import java.util.List;

public interface IRouter<T> {
    /** 不同的processor对{@link sy.databus.entity.signal.ISignal.IRoutingPattern} 存在多态实现*/
    void route(T t) throws Exception;

    List<? extends IRouter<T>> getNextProcessors();
}
