package sy.databus.entity;

import lombok.Getter;
import sy.databus.process.analyse.ReadingMode;

public class DatabaseReadingTask extends STask {
    @Getter
    protected final ReplayTaskItem replayTaskItem;
    @Getter
    private final ReadingMode mode;

    public DatabaseReadingTask(ProcessorId initiator,
                               ReplayTaskItem item,
                               ReadingMode mode,
                               LoopLockedTask closed) {
        super(item.getTaskUid(), initiator);
        this.replayTaskItem = item;
        this.mode = mode;

        this.closedLoop = closed;
    }

}
