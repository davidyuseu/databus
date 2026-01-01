package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.organize.TaskManager;
import sy.databus.process.IRouter;

import java.util.UUID;

public class TASK_CREATE extends TASK_SIG {

    protected TASK_CREATE(IRouter<IEvent> source, IRoutingPattern routing, STask task) {
        super(source, routing, task);
        TaskManager.addTask(task);
    }

    public UUID getTaskId() {
        return this.task.getTaskId();
    }
}
