package sy.databus.entity.signal;

import lombok.Getter;
import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.process.IRouter;

public class TASK_SIG extends Signal {
    @Getter
    protected final STask task;

    protected TASK_SIG(IRouter<IEvent> source, IRoutingPattern routing, STask task) {
        super(source, routing);
        this.task = task;
        this.task.switchCompletionFlag(false);
    }
}
