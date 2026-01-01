package sy.databus.entity.signal;

import lombok.Getter;
import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.process.IRouter;

public class DATA_RECORD_BEGIN extends TASK_CREATE {

    @Getter
    protected final String taskName;

    public DATA_RECORD_BEGIN (STask task, String taskName, IRouter<IEvent> source,
                           IRoutingPattern routing) {
        super(source, routing, task);
        this.taskName = taskName;
    }
}
