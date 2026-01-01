package sy.databus.entity;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteUtil;
import sy.databus.process.analyse.ReadingMode;

import java.nio.MappedByteBuffer;

@Log4j2
@Deprecated
public class AnalysisFileTask extends ReFileTask {

    @Getter
    private final MappedByteBuffer fileBuffer;

    @Getter
    private final ByteBuf fileBuf;

    public AnalysisFileTask(ProcessorId initiator,
                            ReplayFileItem replayFileItem,
                            MappedByteBuffer fileBuffer,
                            ByteBuf fileBuf,
                            ReadingMode mode) {
        super(initiator, replayFileItem, mode);
        this.fileBuffer = fileBuffer;
        this.fileBuf = fileBuf;

        this.closedLoop = () -> {
            if (this.fileBuf != null && this.fileBuf.refCnt() == 1) {
                this.fileBuf.release();
            } else {
                log.warn("The fileBuf should not be null and its refCnt should be 1 when closing the loop task!");
            }

            if (this.fileBuffer != null) {
                SByteUtil.clean(this.fileBuffer);
            } else {
                log.warn("The fileBuffer should not be null when closing the loop task!");
            }
            System.gc();
            this.replayFileItem.setTaskTip("任务文件 " + this.replayFileItem.getNum() + " 已完成解析！");
        };
    }

    public AnalysisFileTask(ProcessorId initiator, ReplayFileItem replayFileItem,
                            MappedByteBuffer fileBuffer, ByteBuf fileBuf,
                            boolean doClosedLoop, ReadingMode mode) {
        this(initiator, replayFileItem, fileBuffer, fileBuf, mode);
        this.doClosedLoop = doClosedLoop;
    }
}
