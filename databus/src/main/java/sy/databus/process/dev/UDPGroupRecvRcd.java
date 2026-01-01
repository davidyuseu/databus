package sy.databus.process.dev;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.shaded.org.jctools.queues.SpscLinkedQueue;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.databus.entity.signal.DATA_RECORD_BEGIN;
import sy.databus.entity.signal.DATA_RECORD_END;
import sy.databus.global.ProcessorType;
import sy.databus.process.Console;
import sy.databus.process.Processor;
import sy.databus.view.watch.MessageSeriesProcessorWatchPane;

import static sy.databus.process.Console.Config.STATIC;

@Log4j2
@Processor(
        type = ProcessorType.UDP_MULTICAST,
        pane = MessageSeriesProcessorWatchPane.class
)
public class UDPGroupRecvRcd extends UDPGroupReceiver {

    public static final int MAX_QUEUE_SIZE = 2048;

    private boolean bRecording = false;

    @Getter
    private final SpscLinkedQueue<PacketRecord> bufferQueue = new SpscLinkedQueue<>();

    @Getter @Setter
    @Console(config = STATIC, display = "通道")
    private ByteBuf channelType;
    @Getter @Setter
    @Console(config = STATIC, display = "机号")
    private ByteBuf craftNum;
    @Getter @Setter
    @Console(config = STATIC, display = "消息号")
    private ByteBuf msgType;

    @Override
    public void initialize() {
        super.initialize();

        appendSlot(DATA_RECORD_BEGIN.class, signal -> {
            bRecording = true;
            return true;
        });
        pileUpSlot(DATA_RECORD_END.class, signal -> {
            bRecording = false;
            bufferQueue.clear();
            return true;
        });
    }

    public record PacketRecord(long timestamp, byte[] content){}

    @Override
    protected void handlePacket(DatagramPacket packet) throws Exception {
        if (bRecording) {
            if (bufferQueue.size() < MAX_QUEUE_SIZE) {
                var buf = packet.content();
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                bufferQueue.offer(new PacketRecord(CachedClock.instance().currentTimeMillis(),
                        bytes));
            } else {
                log.warn("The buffer queue of {} has reached the max limit ({}), give up record this packet!",
                        this.getNameValue(), MAX_QUEUE_SIZE);
            }
        }
        super.handlePacket(packet);
    }
}
