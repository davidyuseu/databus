package sy.databus.process.frame.handler.decoder;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SByteUtil;
import sy.databus.process.Console;
import sy.databus.process.frame.handler.Handler;

import static sy.databus.process.Console.Config.STATIC;
import static sy.databus.process.frame.handler.Handler.Category.HANDLER_FRAME;

@Handler(
        category = HANDLER_FRAME,
        group = "decoder",
        name = "帧头帧尾搜帧"
)
@Log4j2
public class HeaderTailFrameDecoder extends AbstractLockFrameTrans {

    private final int MAX_LEN = 65536; // 异常帧长

    @Getter @Setter
    @Console(config = STATIC, display = "帧尾")
    private ByteBuf tail = null;
    private int tailLen;

    @Setter@Getter
    @Console(config = STATIC, display = "帧头")
    private ByteBuf header = null;
    private int headerLen;

    private boolean headerFounded = false;
    private int headerOffset = -1; // 帧头相对于readerIndex的偏移量
    private int lockOffset = 0; // 找到帧头后当前搜帧的索引相对于readerIndex的偏移量

    public HeaderTailFrameDecoder() {}

    public HeaderTailFrameDecoder(ByteBuf header, ByteBuf tail) {
        this.header = header;
        this.tail = tail;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void boot() {
        super.boot();
        if (header != null)
            headerLen = header.capacity();
        if (tail != null)
            tailLen = tail.capacity();
    }

    @Override
    protected void decode(ByteBuf cumulation) throws Exception {
        ByteBuf decoded = lockFrame(cumulation);
        if (decoded != null) {
            out.add(decoded);
        }
    }

    /** 因为cumulation的capacity和readerIndex是动态变化的，
     * 所以header的索引headerIndex和lockIndex也会随着readerIndex的变化而变化，
     * 故应使用相对帧头的偏移量*/
    protected ByteBuf lockFrame(ByteBuf cumulation) throws Exception {
        if (!headerFounded) {
            headerOffset = SByteUtil.indexOf(cumulation, header) - cumulation.readerIndex();
            if (headerOffset < 0) { // 没有找到帧头直接退出，等下一次搜帧
                cumulation.readerIndex(cumulation.writerIndex());
                return null;
            } else {
                headerFounded = true;
                lockOffset = headerOffset + headerLen;
            }
        }
        // 能走到这步说明找到了帧头，foundHeader 为true，开始找帧尾
        int tailIndex = SByteUtil.indexOf(cumulation, tail, cumulation.readerIndex() + lockOffset);
        if (tailIndex < 0) {
            int len = cumulation.readableBytes() - headerOffset;
            if (len > MAX_LEN) {// 找到帧头，但一直没找到帧尾，长度超限了，报错
                fail(len);
                headerFounded = false;
                cumulation.readerIndex(cumulation.writerIndex());
                return null;
            }
            lockOffset = cumulation.writerIndex() - cumulation.readerIndex();
            return null;
        } else {
            int headerIndex = cumulation.readerIndex() + headerOffset;
            int len = tailIndex - headerIndex + tailLen;
            cumulation.readerIndex(headerIndex);
            headerFounded = false;
            return cumulation.readRetainedSlice(len);
        }
    }

    private void fail(long frameLength) {
        String err;
        if (frameLength > 0) {
            err = "frame length exceeds " + MAX_LEN +
                    ": " + frameLength + " - discarded";
        } else {
            err = "frame length exceeds " + MAX_LEN +
                    " - discarding";
        }
        log.error(err);
        throw new TooLongFrameException(err);
    }
}
