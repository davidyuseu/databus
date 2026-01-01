package sy.databus.entity.signal;

import lombok.Getter;
import sy.databus.entity.IEvent;
import sy.databus.entity.ReFileTask;
import sy.databus.process.IRouter;
import sy.databus.process.analyse.ReadingMode;

public class FILE_READ_BEGIN extends DATA_TASK_BEGIN {

    @Getter
    private ReadingMode mode;
    public FILE_READ_BEGIN(ReadingMode mode, ReFileTask fileTask, IRouter<IEvent> source,
                           IRoutingPattern routing) {
        super(fileTask, fileTask.getReplayFileItem().getName(), source, routing);
        this.mode = mode;
    }
}
