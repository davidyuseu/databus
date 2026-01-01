package sy.databus.entity.signal;

import sy.databus.entity.DatabaseReadingTask;
import sy.databus.entity.IEvent;
import sy.databus.process.IRouter;

public class DATABASE_READ_END extends DATA_TASK_END {

    public DATABASE_READ_END(IRouter<IEvent> source, DatabaseReadingTask task) {
        super(source, task);
    }
}
