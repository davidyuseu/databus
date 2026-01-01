package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.STask;
import sy.databus.organize.TaskManager;
import sy.databus.process.IRouter;

public class TASK_LOOPBACK extends TASK_FINISH {

    protected TASK_LOOPBACK(IRouter<IEvent> source, STask task) {
        super(source, task);
        TaskManager.addTask(task);
    }
}
