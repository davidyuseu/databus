package sy.databus.process.internal;

import com.google.common.collect.EvictingQueue;
import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteBufLogger;
import sy.databus.OriDataViewController;
import sy.databus.entity.message.IMessage;
import sy.databus.entity.signal.FILE_READ_BEGIN;
import sy.databus.process.analyse.ReadingMode;
import sy.databus.process.frame.AbstractTransHandler;

import java.util.List;

import static sy.databus.OriDataViewController.DEFAULT_DATA_VIEW_MAX_LEN;
import static sy.databus.OriDataViewTask.COUNT_ORI_DATA_VIEW;

/**
 * {@link OriDataViewHandler} 不要在各父处理器间复用，否则插入新处理器时可能会将上一个处理器的消息传递进去，
 * 所以{@link #locker}应该是全局的，防止临界区混乱，比如UI刷新线程拿到了上一个handler中的锁，而对当前handler
 * 中的{@link #strQueue}进行了操作
 * */
@Log4j2
public class OriDataViewHandler extends AbstractTransHandler<IMessage<ByteBuf>> {

    @Getter
    private EvictingQueue<String> strQueue = EvictingQueue.create(COUNT_ORI_DATA_VIEW);

    private StringBuilder hexBuilder = new StringBuilder(2048);

    @Setter
    private boolean loaded = true;

    @Getter
    private final Object locker;
    @Getter
    private final OriDataViewController controller;

    public OriDataViewHandler(Object locker, OriDataViewController controller) {
        this.locker = locker;
        this.controller = controller;
        /** 对于即插即用的InternalHandler，若没有专门initialize，则应该在构造器中初始化*/
        initialize();
    }

    @Override
    public void initialize() {
        /** 槽处理：当处于文件分析的ALL_IN模式时，将原码显示handler从其父处理器中卸载掉*/
        appendSlot(FILE_READ_BEGIN.class, signal -> {
            if (((FILE_READ_BEGIN) signal).getMode()
                    == ReadingMode.ALLIN) {
                loaded = false; // 在FX线程卸载前就透传消息，最大化性能
                Platform.runLater(controller::unload);
                log.info("ALL_IN模式下卸载原码显示！");
            }
            return true;
        });
    }
    
    public List<String> getListAndClear() {
        synchronized (locker) {
            var list = strQueue.stream().toList();
            strQueue.clear();
            return list;
        }
    }

    @Setter
    private volatile int offset = 0;
    @Setter
    private volatile int maxLen = DEFAULT_DATA_VIEW_MAX_LEN;

    @Override
    public void handle(IMessage<ByteBuf> byteBufIMessage) throws Exception {
        synchronized (locker) {
            if (loaded) {
                ByteBuf buf = byteBufIMessage.getData();
                SByteBufLogger.dumpOriBytes(hexBuilder, buf, offset, maxLen);
                if (!hexBuilder.isEmpty()) {
                    String hexStr = hexBuilder.toString();
                    hexBuilder.setLength(0);
                    strQueue.add(hexStr);
                }
            }
        }
        fireNext(byteBufIMessage);
    }
}
