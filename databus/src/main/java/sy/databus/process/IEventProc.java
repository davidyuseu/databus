package sy.databus.process;

import sy.databus.entity.signal.ISignal;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IEventProc<T> {
    void handle(T t) throws Exception;

    Map<Class<? extends ISignal>, List<Function<ISignal, Boolean>>> getSlots();

    void pileUpSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot);

    void appendSlot(Class<? extends ISignal> sig, Function<ISignal, Boolean> slot);
}
