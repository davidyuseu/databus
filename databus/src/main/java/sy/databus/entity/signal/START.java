package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

public final class START extends Signal {

    public START(IRouter<IEvent> source, IRoutingPattern routing) {
        super(source, routing);
    }
}
