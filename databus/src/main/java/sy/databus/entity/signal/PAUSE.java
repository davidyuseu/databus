package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

public class PAUSE extends Signal {

    public PAUSE(IRouter<IEvent> source, IRoutingPattern routing) {
        super(source, routing);
    }
}
