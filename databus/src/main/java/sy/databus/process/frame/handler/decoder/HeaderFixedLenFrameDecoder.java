package sy.databus.process.frame.handler.decoder;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sy.common.util.SByteUtil;
import sy.databus.process.Console;
import sy.databus.process.ProcessorInitException;
import io.netty.buffer.ByteBuf;
import sy.databus.process.frame.handler.Handler;

import java.util.Objects;

import static io.netty.util.internal.ObjectUtil.checkPositive;
import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "decoder",
        name = "固定帧头定长搜帧"
)
public class HeaderFixedLenFrameDecoder extends AbstractLockFrameTrans {
    @Getter @Setter
    @Console(config = STATIC, display = "帧头")
    private ByteBuf header = null;
    @Getter @Setter
    @Console(config = STATIC, display = "帧长")
    private int frameLength = 1;

    private boolean foundHeader = false;
    private int headerOffset = -1; // 帧头相对于readerIndex的偏移量

    public HeaderFixedLenFrameDecoder() {}

    public HeaderFixedLenFrameDecoder(ByteBuf header, int frameLength) {
        this.header = header;
        this.frameLength = frameLength;
    }

    @SneakyThrows
    public HeaderFixedLenFrameDecoder(String header, int frameLength) {
        this.header = SByteUtil.getBufByString(header);
        this.frameLength = frameLength;
    }

    @Override
    public void initialize() {
        name = "固定帧头定长搜帧";
    }

    @Override
    public void boot() {
        super.boot();
        checkParams();
    }

    public void checkParams() {
        Objects.requireNonNull(header);
        checkPositive(frameLength, "frameLen");
        if(frameLength < header.readableBytes())
            throw new ProcessorInitException("HeaderFixedLenFrameDecoder's frameLength can not be less than the length of its header!");
    }

    @Override
    protected void decode(ByteBuf cumulation) throws Exception {
        ByteBuf decoded = lockFrame(cumulation);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    protected ByteBuf lockFrame(ByteBuf cumulation) throws Exception {
        if (!foundHeader) {
            headerOffset = SByteUtil.indexOf(cumulation, header) - cumulation.readerIndex();
            if (headerOffset < 0) {
                cumulation.readerIndex(cumulation.writerIndex());
                return null;
            } else {
                foundHeader = true;
            }
        }
        if (cumulation.readableBytes() - headerOffset < frameLength) {
            return null;
        } else {
            cumulation.readerIndex(cumulation.readerIndex() + headerOffset);
            foundHeader = false;
            return cumulation.readRetainedSlice(frameLength);
        }
    }

}
