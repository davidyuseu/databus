package sy.databus.process.analyse;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import sy.databus.entity.message.EfficientMessage;
import sy.databus.entity.message.metadata.Metadata;
import sy.databus.global.ProcessorType;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.view.watch.FixedLenDataPumpWatchPane;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import static sy.databus.process.Console.Config.STATIC;


@Log4j2
@Processor(
        type = ProcessorType.ORDINARY_FILE_READER,
        pane = FixedLenDataPumpWatchPane.class,
        fieldsIgnore = {"toPreferDev"}
)
public class FixedLenDataPump extends FileReplayer {

    @Getter @Setter
    @Console(config = STATIC, display = "单次抽取帧长")
    private int fixedLen = 1024;

    @Getter @Setter
    @Console(config = STATIC, display = "文件类型")
    private String extension = "*.dat";

    @Override
    public void initialize() {
        super.initialize();
        initializeName("定长数据泵");
    }

    @SneakyThrows
    @Override
    protected void readBuffer(FileChannel fileChannel) {
        long remained = fileChannel.size() - fileChannel.position() - 1;
        if (remained < 1 || nextProcessors.size() < 1)
            return;
        long count = 0;
        do {
            if (!workingLoop) { // close()
                return;
            }
            int len = remained > fixedLen ? fixedLen : (int) remained;
            for (int i = 0; i < nextProcessors.size(); i++) {
                next = nextProcessors.get(i);
                ByteBuf msgBuf = PooledByteBufAllocator.DEFAULT.directBuffer(len);
                msgBuf.writerIndex(len);
                fileChannel.read(msgBuf.nioBuffer());
                if (i != nextProcessors.size() - 1)
                    fileChannel.position(fileChannel.position() - len);
                EfficientMessage msg = new EfficientMessage(new Metadata(count), msgBuf);
                try {
                    fireNext(msg);
                    next = null;
                } catch (Exception e) {
                    log.error(e);
                    handleExceptionMsg(msg);
                    return;
                }
            }
            count++;
            remained = fileChannel.size() - fileChannel.position() - 1;
        } while (remained > 0);
    }

    @Override
    protected List<TimeStake> getTimeStakes(FileChannel fileChannel) {
        // empty impl
        return null;
    }

    @Override
    protected void replayBuffer(FileChannel fileChannel) throws IOException {
        // empty impl
    }
}
