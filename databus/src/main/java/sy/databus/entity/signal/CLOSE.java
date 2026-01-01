package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

public class CLOSE extends Signal {

    public CLOSE(IRouter<IEvent> source, IRoutingPattern routing) {
        super(source, routing);
    }
}
