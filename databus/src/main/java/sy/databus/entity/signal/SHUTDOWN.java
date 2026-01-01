package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

public class SHUTDOWN extends Signal {

    public SHUTDOWN(IRouter<IEvent> source, IRoutingPattern routing) {
        super(source, routing);
    }
}
