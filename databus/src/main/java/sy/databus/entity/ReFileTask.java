package sy.databus.entity;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SFileUtils;
import sy.databus.process.analyse.ReadingMode;

import java.io.File;

@Log4j2
public class ReFileTask extends STask {
    @Getter
    protected final ReplayFileItem replayFileItem;
    @Getter
    private final ReadingMode mode;

    public ReFileTask(ProcessorId initiator, ReplayFileItem replayFileItem, ReadingMode mode) {
        super(replayFileItem.getUuid(), initiator);
        this.replayFileItem = replayFileItem;
        this.mode = mode;

        this.closedLoop = () -> {
            if (ReadingMode.ALLIN == mode) {
                this.replayFileItem.setTaskTip("任务文件 " + this.replayFileItem.getNum() + " 已完成处理！");
                log.info("任务文件已完成处理：{}_{}",
                        this.replayFileItem.getNum(), this.replayFileItem.getName());
            }
            File cacheDir = new File(".\\dataFusionCaches");
            SFileUtils.deleteFiles(cacheDir);
            System.gc();
        };
    }
}
