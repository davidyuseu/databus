package sy.databus.process.frame;

import io.netty.buffer.ByteBuf;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.signal.DATA_TASK_BEGIN;
import sy.databus.entity.signal.DATA_TASK_END;
import sy.databus.entity.signal.ISignal;
import sy.databus.global.Constants;
import sy.databus.global.ProcessorType;
import sy.databus.process.AbstractHandler;
import sy.databus.process.Processor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Log4j2
@Processor(
        type = ProcessorType.FILE_DATA_WRITER,
        pane = MessageSeriesProcessorWatchPane.class
)
public class OriDataWriter extends MessageSeriesProcessor<ByteBuf> {
    private FileChannel fileChannel = null;

    protected final DateFormat dateFormat2Ms4File = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.CHINA); //"yyyyMMdd_HHmmss-SSS"
    protected final DateFormat dateFormat2Day4File = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);

    @Override
    public void initialize() {
        super.initialize();

        setNameValue("原码记录器");

        appendSlot(DATA_TASK_BEGIN.class, this::excTask);

        pileUpSlot(DATA_TASK_END.class, sig -> {
            finish();
            return true;
        });

        setTailHandler(new AbstractHandler<>() {

            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                fileChannel.write(msg.getData().nioBuffer());
                DEFAULT_TAIL.handle(msg);
            }

            @Override
            public void initialize() {
                name = "记录原码";
            }
        });
    }

    private void finish() {
        if (fileChannel != null) {
            try {
                fileChannel.close();
                fileChannel = null;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private boolean excTask(ISignal signal) {
        finish();

        String reFileName = "ori_writer";
        if (signal instanceof DATA_TASK_BEGIN sig)
            reFileName = sig.getTaskName();
        String dirPath = Constants.CLASSIFYING_DIR_PATH;
        dirPath += dateFormat2Day4File.format(CachedClock.instance().currentTimeMillis()) + "\\"
                + reFileName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dirPath + "\\"
                + getNameValue() + "_"
                + dateFormat2Ms4File.format(CachedClock.instance().currentTimeMillis()) + ".dat");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
}
