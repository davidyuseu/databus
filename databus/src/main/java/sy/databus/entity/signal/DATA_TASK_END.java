package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.process.IRouter;

public class DATA_TASK_END extends TASK_FINISH{

    public DATA_TASK_END(IRouter<IEvent> source, STask task) {
        super(source, task);
    }
}
