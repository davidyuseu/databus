package sy.databus.process.frame;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.message.EfficientMessage;
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
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Log4j2
@Processor(
        type = ProcessorType.FILE_DATA_WRITER,
        pane = MessageSeriesProcessorWatchPane.class
)
public class ASCIIMsgWriter extends MessageSeriesProcessor<ByteBuf> {

    private FileWriter fileWriter = null;

    protected final DateFormat dateFormat2Ms4File = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.CHINA); //"yyyyMMdd_HHmmss-SSS"
    protected final DateFormat dateFormat2Day4File = new SimpleDateFormat("yyyy_MM_dd", Locale.CHINA);

    @Override
    public void initialize() {
        super.initialize();

        setNameValue("ASCII原码记录器");

        appendSlot(DATA_TASK_BEGIN.class, this::excTask);

        pileUpSlot(DATA_TASK_END.class, sig -> {
            finish();
            return true;
        });

        setTailHandler(new AbstractHandler<>() {

            @Override
            public void handle(IMessage<ByteBuf> msg) throws Exception {
                if (msg instanceof EfficientMessage message) {
                    String time = dateFormat2Ms4File.format(message.getMetadata().getBirthTimestamp());
                    fileWriter.write(time);
                    fileWriter.write(message.getData().toString(CharsetUtil.UTF_8));
                    DEFAULT_TAIL.handle(msg);
                } else {
                    log.error("Unsupported msg type!");
                    msg.release();
                }
            }

            @Override
            public void initialize() {
                name = "记录ASCII码";
            }
        });
    }

    private boolean excTask(ISignal signal) {
        finish();

        String reFileName = "ascii_writer";
        if (signal instanceof DATA_TASK_BEGIN sig)
            reFileName = sig.getTaskName();
        String dirPath = Constants.EXCEL_DIR_PATH;
        dirPath += dateFormat2Day4File.format(CachedClock.instance().currentTimeMillis()) + "\\"
                + reFileName;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dirPath + "\\"
                + getNameValue() + "_"
                + dateFormat2Ms4File.format(CachedClock.instance().currentTimeMillis()) + ".txt");
        if (file.exists())
            file.delete();
        try {
            file.createNewFile();
            fileWriter = new FileWriter(file, Constants.gCharset);
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public void finish() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
                fileWriter = null;
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
