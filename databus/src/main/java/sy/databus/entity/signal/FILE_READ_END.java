package sy.databus.entity.signal;

import sy.databus.entity.IEvent;
import sy.databus.entity.ReFileTask;
import sy.databus.process.IRouter;

public class FILE_READ_END extends DATA_TASK_END {

    public FILE_READ_END(IRouter<IEvent> source, ReFileTask fileItem) {
        super(source, fileItem);
    }
}
