package sy.databus.entity.signal;

import lombok.Getter;
import sy.databus.entity.DatabaseReadingTask;
import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;
import sy.databus.process.analyse.ReadingMode;

public class DATABASE_READ_BEGIN extends DATA_TASK_BEGIN {

    @Getter
    private final ReadingMode mode;

    public DATABASE_READ_BEGIN(DatabaseReadingTask task, IRouter<IEvent> source, IRoutingPattern routing) {
        super(task, task.getReplayTaskItem().getTaskTable(), source, routing);
        this.mode = task.getMode();
    }

}
